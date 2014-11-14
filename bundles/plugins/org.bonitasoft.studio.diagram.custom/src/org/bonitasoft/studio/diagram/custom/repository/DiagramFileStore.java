/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.diagram.custom.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.studio.common.editor.EditorUtil;
import org.bonitasoft.studio.common.emf.tools.EMFResourceUtil;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.common.repository.filestore.EMFFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.diagram.custom.Activator;
import org.bonitasoft.studio.diagram.custom.i18n.Messages;
import org.bonitasoft.studio.migration.model.report.MigrationReportPackage;
import org.bonitasoft.studio.migration.model.report.Report;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.diagram.edit.parts.PoolEditPart;
import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditor;
import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditorUtil;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.FeatureNotFoundException;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.ui.services.editor.EditorService;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Romain Bioteau
 *
 */
public class DiagramFileStore extends EMFFileStore implements IRepositoryFileStore {

    public static final String PROC_EXT = "proc";

    private final NotificationListener poolListener = new PoolNotificationListener();

    public DiagramFileStore(final String fileName, final IRepositoryStore<? extends EMFFileStore> store) {
        super(fileName, store);
    }

    @Override
    public synchronized MainProcess getContent() {
        return (MainProcess) super.getContent();
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.filestore.EMFFileStore#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        final DiagramRepositoryStore store =  (DiagramRepositoryStore) getParentStore();
        String label = null;
        final String fileName = getName();
        label = fileName.replace("."+getResource().getFileExtension(), "");
        String[] eObectIfFromEObjectType = null;
        EMFResourceUtil emfResourceUtil = null;
        try {
            emfResourceUtil = new EMFResourceUtil(getResource().getLocation().toFile());
            eObectIfFromEObjectType = emfResourceUtil.getEObectIfFromEObjectType("process:MainProcess");
        } catch (final FeatureNotFoundException e) {
            BonitaStudioLog.error(e);
            return label;
        }

        if(eObectIfFromEObjectType != null && eObectIfFromEObjectType.length == 1){
            final String diagramUUID = eObectIfFromEObjectType[0];
            label = store.getLabelFor(diagramUUID);
            if(label == null){
                label = fileName.replace("."+getResource().getFileExtension(), "");
            }
        }
        return label;
    }

    @Override
    protected URI getResourceURI() {
        final IPath fullPath = getResource().getFullPath();
        return URI.createPlatformResourceURI(fullPath.toOSString(), true);
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.filestore.EMFFileStore#getIcon()
     */
    @Override
    public Image getIcon() {
        return Pics.getImage("ProcessDiagramFile.gif", Activator.getDefault());
    }

    @Override
    public Resource getEMFResource() {
        final DiagramEditor editor = getOpenedEditor();
        if(editor != null){
            final DiagramEditPart diagramEditPart = editor.getDiagramEditPart();
            if(diagramEditPart != null){
                final EObject resolveSemanticElement = diagramEditPart.resolveSemanticElement();
                if(resolveSemanticElement != null && resolveSemanticElement.eResource() != null){
                    return resolveSemanticElement.eResource();
                }
            }
        }
        return super.getEMFResource();
    }

    public void registerListeners(final EObject input,final TransactionalEditingDomain domain) {
        DiagramEventBroker.getInstance(domain).addNotificationListener(input, ProcessPackage.Literals.CONTAINER__ELEMENTS, poolListener ) ;
        if(input instanceof MainProcess){
            for(final EObject element : ((MainProcess) input).getElements()){
                if(element instanceof Pool){
                    DiagramEventBroker.getInstance(domain).addNotificationListener(element, ProcessPackage.Literals.ELEMENT__NAME, poolListener ) ;
                    DiagramEventBroker.getInstance(domain).addNotificationListener(element, ProcessPackage.Literals.ABSTRACT_PROCESS__VERSION, poolListener ) ;
                }
            }
        }
    }


    public void unregisterListeners(final EObject input,final TransactionalEditingDomain domain) {
        DiagramEventBroker.getInstance(domain).removeNotificationListener(input, ProcessPackage.Literals.CONTAINER__ELEMENTS, poolListener) ;
    }

    public DiagramEditor getOpenedEditor(){
        if(PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null){
            final String resourceName = getResource().getName();
            for (final IEditorReference ref:PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()){
                String inputName;
                try {
                    inputName = ref.getEditorInput().getName();
                    if (resourceName.equals(inputName)){
                        final IEditorPart editor = ref.getEditor(false);
                        if(editor instanceof ProcessDiagramEditor){
                            return (DiagramEditor) editor;
                        }
                    }
                } catch (final PartInitException e) {
                    BonitaStudioLog.error(e,Activator.PLUGIN_ID);
                }
            }
        }
        return null;
    }

    public List<AbstractProcess> getProcesses()  {
        MainProcess diagram = null;
        final DiagramEditor editor = getOpenedEditor();
        if (editor != null && editor.getDiagramEditPart() != null) {
            diagram = (MainProcess) editor.getDiagramEditPart().resolveSemanticElement();
        }
        if(diagram == null){
            diagram = getContent() ;
        }
        final List<AbstractProcess> allProcesses = ModelHelper.getAllProcesses(diagram);
        final List<AbstractProcess> pools = new ArrayList<AbstractProcess>();
        for(final AbstractProcess abstractProcess : allProcesses){
            if(abstractProcess instanceof Pool){
                pools.add(abstractProcess);
            }
        }
        return pools;
    }

    @Override
    protected void doSave(final Object content) {
        final Resource resource = getEMFResource() ;
        final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(resource);
        try {
            OperationHistoryFactory.getOperationHistory().execute(new SaveDiagramResourceCommand(content, editingDomain, resource), Repository.NULL_PROGRESS_MONITOR,
                    null);
        } catch (final ExecutionException e1) {
            BonitaStudioLog.error(e1);
        }
        if (content instanceof DiagramDocumentEditor) {
            ((DiagramDocumentEditor)content).doSave(Repository.NULL_PROGRESS_MONITOR);
        }

        try {
            resource.save(ProcessDiagramEditorUtil.getSaveOptions()) ;
        } catch (final IOException e) {
            BonitaStudioLog.error(e) ;
        }
    }


    @Override
    protected IWorkbenchPart doOpen() {
        final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        closeEditorIfAlreadyOpened(activePage);
        IEditorPart part = null;
        try {
            final EditingDomain editingDomain = getParentStore().getEditingDomain(getEMFResource().getURI());
            part = EditorService.getInstance().openEditor(new URIEditorInput(EcoreUtil.getURI(ModelHelper.getDiagramFor(getContent(), editingDomain))));
            if(part instanceof DiagramEditor){
                final DiagramEditor editor = (DiagramEditor) part;
                final MainProcess diagram = (MainProcess) editor.getDiagramEditPart().resolveSemanticElement() ;
                diagram.eAdapters().add(new PoolNotificationListener());
                if(isReadOnly()){
                    setReadOnlyAndOpenWarningDialogAboutReadOnly(editor);
                }
                registerListeners(diagram, editor.getEditingDomain()) ;
                final IGraphicalEditPart editPart = editor.getDiagramEditPart().getChildBySemanticHint(PoolEditPart.VISUAL_ID+"");
                if(editPart != null) {
                    editor.getDiagramEditPart().getViewer().select(editPart);
                }

                handleMigrationReportIfPresent(activePage);
                editor.setFocus(); //refresh coolbar button
                return editor;
            }
        } catch (final PartInitException e) {
            BonitaStudioLog.error(e) ;
        }
        return part ;
    }


    private void setReadOnlyAndOpenWarningDialogAboutReadOnly(final DiagramEditor editor) {
        editor.getDiagramEditPart().disableEditMode() ;
        if (editor instanceof ProcessDiagramEditor) {
            ((ProcessDiagramEditor) editor).setReadOnly(true);
        }
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.readOnlyFileTitle, Messages.readOnlyFileWarning);
            }
        });
    }

    @Override
    public void setReadOnly(final boolean readOnly) {
        super.setReadOnly(readOnly);
        final DiagramEditor openedEditor = getOpenedEditor();
        if (openedEditor != null) {
            if (openedEditor.getDiagramEditPart() != null) {
                if (readOnly) {
                    openedEditor.getDiagramEditPart().disableEditMode();
                } else {
                    openedEditor.getDiagramEditPart().enableEditMode();
                }
            }
            if (openedEditor instanceof ProcessDiagramEditor) {
                ((ProcessDiagramEditor) openedEditor).setReadOnly(readOnly);
            }
        }
    }

    private void handleMigrationReportIfPresent(final IWorkbenchPage activePage)
            throws PartInitException {
        if(hasMigrationReport()){
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    try {
                        activePage.showView("org.bonitasoft.studio.migration.view");
                    } catch (final PartInitException e) {
                        BonitaStudioLog.error(e);
                    }
                }
            });
        } else {
            final IViewPart migrationView = activePage.findView("org.bonitasoft.studio.migration.view");
            if(migrationView != null){
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(migrationView);
            }
        }
    }

    private void closeEditorIfAlreadyOpened(final IWorkbenchPage activePage) {
        final MainProcess newProcess = getContent();
        for (final IEditorReference editor : activePage.getEditorReferences()) {
            final IEditorPart simpleEditor = editor.getEditor(true);
            if (simpleEditor instanceof DiagramEditor) {
                final DiagramEditor diagramEditor = (DiagramEditor) simpleEditor;
                final EObject input = diagramEditor.getDiagramEditPart().resolveSemanticElement();
                MainProcess oldProcess = null;
                if (input instanceof MainProcess) {
                    oldProcess =(MainProcess) input;
                }else if(input instanceof Form){
                    oldProcess = ModelHelper.getMainProcess(input);
                }

                if(oldProcess != null){
                    if(newProcess != null){
                        if (oldProcess.getName().equals(newProcess.getName()) &&
                                oldProcess.getVersion().equals(newProcess.getVersion())) {
                            activePage.closeEditor(diagramEditor, false);
                        }
                    } else {
                        BonitaStudioLog.log("The new Process is null. Name of currentDiagramFileStore is: " +getName());
                    }
                }
            }
        }
    }

    @Override
    protected void doClose() {
        final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if(activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null){
            final IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
            // look for the resource in other editors
            for (final IEditorReference iEditorReference : editors) {
                try {
                    final IEditorInput input = iEditorReference.getEditorInput();
                    final IResource iResource = EditorUtil.retrieveResourceFromEditorInput(input);
                    if (getResource().equals(iResource)) {
                        final IWorkbenchPart part = iEditorReference.getPart(false);
                        if(part != null && part instanceof DiagramDocumentEditor){
                            ((DiagramDocumentEditor)part).close(true);
                        }
                    }
                } catch (final PartInitException e) {
                    // no input? -> nothing to do
                }
            }
        }
        super.doClose();
    }

    @Override
    public IFile getResource() {
        return getParentStore().getResource().getFile(getName());
    }


    /**
     *
     * @return the migration report if exists otherwise returns null
     */
    public Report getMigrationReport(){
        final Resource emfResource = getEMFResource();
        if(emfResource != null){
            for(final EObject root : emfResource.getContents()){
                if(root instanceof Report){
                    return (Report) root;
                }
            }
        }
        return null;
    }

    /**
     *
     * Remove the migration report from the proc file.
     * @param save , Whether we save the resoruce after deletion or not
     * @throws IOException
     */
    public void clearMigrationReport(final boolean save) throws IOException{
        final EObject toRemove = null;
        final Resource emfResource = getEMFResource();
        final Report report = getMigrationReport();
        if(report != null){
            final TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(toRemove);
            if(domain != null){
                domain.getCommandStack().execute(new RecordingCommand(domain) {
                    @Override
                    protected void doExecute() {
                        emfResource.getContents().remove(report);
                    }
                });
                if(save){
                    emfResource.save(Collections.emptyMap());
                }
            }
        }
    }

    public boolean hasMigrationReport() {
        final EMFResourceUtil emfResourceUtil = new EMFResourceUtil(getResource().getLocation().toFile());
        final Map<String, String[]> featureValueFromEObjectType = emfResourceUtil.getFeatureValueFromEObjectType("report:Report", MigrationReportPackage.Literals.REPORT__NAME);
        if(featureValueFromEObjectType == null || featureValueFromEObjectType.values() == null || featureValueFromEObjectType.values().isEmpty()){
            return false;
        }
        final Iterator<String[]> iterator = featureValueFromEObjectType.values().iterator();
        final String[] values = iterator.next();
        return values != null && values[0] != null;
    }

    public boolean isOpened() {
        return getOpenedEditor() != null;
    }

}
