/**
 * Copyright (C) 2009 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
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
package org.bonitasoft.studio.properties.sections.general;

import java.util.List;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.properties.ExtensibleGridPropertySection;
import org.bonitasoft.studio.common.properties.IExtensibleGridPropertySectionContribution;
import org.bonitasoft.studio.decision.ui.DecisionTableWizard;
import org.bonitasoft.studio.decision.ui.condition.MaximizableWizardDialog;
import org.bonitasoft.studio.decision.ui.condition.TakeTransitionLabelProvider;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.viewer.ExpressionViewer;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.process.ANDGateway;
import org.bonitasoft.studio.model.process.BoundaryEvent;
import org.bonitasoft.studio.model.process.CatchLinkEvent;
import org.bonitasoft.studio.model.process.Connection;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.SequenceFlow;
import org.bonitasoft.studio.model.process.SequenceFlowConditionType;
import org.bonitasoft.studio.model.process.SourceElement;
import org.bonitasoft.studio.model.process.TargetElement;
import org.bonitasoft.studio.model.process.decision.DecisionTable;
import org.bonitasoft.studio.model.process.decision.DecisionTableAction;
import org.bonitasoft.studio.model.process.decision.DecisionTableCondition;
import org.bonitasoft.studio.model.process.decision.DecisionTableLine;
import org.bonitasoft.studio.model.process.decision.transitions.TakeTransitionAction;
import org.bonitasoft.studio.model.process.decision.transitions.TransitionsFactory;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.bonitasoft.studio.profiles.manager.BonitaProfilesManager;
import org.bonitasoft.studio.profiles.manager.IBonitaActivitiesCategory;
import org.bonitasoft.studio.properties.i18n.Messages;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditObservables;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;


/**
 * @author Mickael Istria
 * @author Aurelien Pupier
 * @author Romain Bioteau
 */
public class TransitionCondition implements IExtensibleGridPropertySectionContribution {


    private TransactionalEditingDomain editingDomain;
    private SequenceFlow transition;
    private ExpressionViewer conditionViewer;
    private Color color;
    private Button defaultCheckBox;
    private EMFDataBindingContext dataBindingContext;
    private Button useExpressionCondition;
    private Button useDecisionTable;
    private TabbedPropertySheetWidgetFactory widgetFactory;
    private Section conditionSection;


    @Override
    public void createControl(final Composite mainComposite,final TabbedPropertySheetWidgetFactory widgetFactory,
            ExtensibleGridPropertySection extensibleGridPropertySection) {

        this.widgetFactory = widgetFactory ;
        mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 5).create());
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true,true).create()) ;


        defaultCheckBox = widgetFactory.createButton(mainComposite, Messages.defaultFlowLabel, SWT.CHECK);
        defaultCheckBox.setLayoutData(GridDataFactory.fillDefaults().grab(true,false).create());

        final Composite radioComposite = widgetFactory.createComposite(mainComposite) ;
        radioComposite.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL,SWT.CENTER).grab(true, false).create());
        radioComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).create()) ;

        useExpressionCondition = widgetFactory.createButton(radioComposite, Messages.useExpression, SWT.RADIO) ;
        useDecisionTable = widgetFactory.createButton(radioComposite, Messages.useDecisionTable, SWT.RADIO) ;


        conditionSection = widgetFactory.createSection(mainComposite, Section.NO_TITLE | Section.CLIENT_INDENT) ;
        conditionSection.setLayoutData(GridDataFactory.fillDefaults().grab(true,false).create()) ;
        conditionSection.setExpanded(true) ;

        if(transition.getConditionType() == SequenceFlowConditionType.EXPRESSION){
            conditionSection.setClient(createExpressionComposite(conditionSection, widgetFactory)) ;
        }else{
            conditionSection.setClient(createDecisionTableComposite(conditionSection, widgetFactory)) ;
        }

        refreshBindings() ;
    }


    protected Composite createExpressionComposite(final Composite parent,
            TabbedPropertySheetWidgetFactory widgetFactory) {

        final Composite client = widgetFactory.createComposite(parent) ;
        client.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0,0).spacing(0, 0).create()) ;
        client.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;

        conditionViewer = new ExpressionViewer(client,SWT.BORDER, widgetFactory,editingDomain, ProcessPackage.Literals.SEQUENCE_FLOW__CONDITION);
        conditionViewer.addFilter(new AvailableExpressionTypeFilter(new String[]{ExpressionConstants.CONSTANT_TYPE,ExpressionConstants.PARAMETER_TYPE,ExpressionConstants.SCRIPT_TYPE,ExpressionConstants.VARIABLE_TYPE,ExpressionConstants.CONDITION_TYPE}));
        if(transition.getCondition() == null){
            Expression expression =  ExpressionFactory.eINSTANCE.createExpression() ;
            expression.setReturnType(Boolean.class.getName()) ;
            expression.setReturnTypeFixed(true) ;
            editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__CONDITION,expression)) ;
        }
        conditionViewer.setInput(transition) ;
        // conditionViewer.setSelection(new StructuredSelection(transition.getCondition())) ;
        conditionViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL,SWT.CENTER).grab(true, false).create());

        return client ;
    }

    protected Composite createDecisionTableComposite(final Composite parent,
            TabbedPropertySheetWidgetFactory widgetFactory) {
        final Composite client = widgetFactory.createComposite(parent) ;
        client.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).create()) ;
        client.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;



        final Button updateTableButton = widgetFactory.createButton(client, Messages.editDecisionTable ,SWT.PUSH) ;
        updateTableButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                DecisionTableWizard wizard = new DecisionTableWizard(transition, transition.getDecisionTable());
                final TakeTransitionAction takeTransitionAction = TransitionsFactory.eINSTANCE.createTakeTransitionAction();
                takeTransitionAction.setTakeTransition(true);
                final TakeTransitionAction dontTakeTransitionAction = TransitionsFactory.eINSTANCE.createTakeTransitionAction();
                dontTakeTransitionAction.setTakeTransition(false);
                wizard.setAvailableLineActions(new DecisionTableAction[] { takeTransitionAction, dontTakeTransitionAction });
                wizard.setAvailableTableActions(new DecisionTableAction[] { dontTakeTransitionAction, takeTransitionAction });
                wizard.setActionsLabelProvider(new TakeTransitionLabelProvider());
                wizard.setActionsComparer(new IElementComparer() {
                    @Override
                    public int hashCode(Object element) {
                        return element.hashCode();
                    }

                    @Override
                    public boolean equals(Object a, Object b) {
                        if (a instanceof TakeTransitionAction && b instanceof TakeTransitionAction) {
                            return ((TakeTransitionAction)a).isTakeTransition() == ((TakeTransitionAction)b).isTakeTransition();
                        } else {
                            return a.equals(b);
                        }

                    }
                });

                WizardDialog dialog = new MaximizableWizardDialog(Display.getDefault().getActiveShell(), wizard);
                if (dialog.open() == Dialog.OK) {
                    DecisionTable table = wizard.getDecisionTable();
                    CompoundCommand cmd = new CompoundCommand();
                    cmd.append(new SetCommand(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__DECISION_TABLE, table));
                    editingDomain.getCommandStack().execute(cmd);
                    conditionSection.getClient().dispose() ;
                    conditionSection.setClient(createDecisionTableComposite(conditionSection, TransitionCondition.this.widgetFactory)) ;
                    conditionSection.setExpanded(true) ;
                }
            }
        }) ;


        final Composite tableComposite = widgetFactory.createComposite(client) ;
        tableComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create()) ;
        tableComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0,0).spacing(0, 3).create()) ;

        final DecisionTable table = transition.getDecisionTable() ;
        if(table != null && !table.getLines().isEmpty()){
            for(DecisionTableLine line : table.getLines()){
                if(!line.getConditions().isEmpty()){
                    createLine(tableComposite,line.getConditions(),line.getAction()) ;
                }
            }
        }

        return client ;
    }

    private void createLine(Composite parent, List<DecisionTableCondition> conditions, DecisionTableAction decisionTableAction) {
        final Composite row = widgetFactory.createComposite(parent) ;
        row.setLayoutData(GridDataFactory.fillDefaults().grab(true,false).create()) ;
        row.setLayout(RowLayoutFactory.fillDefaults().spacing(0).type(SWT.HORIZONTAL).create()) ;

        for(DecisionTableCondition condition : conditions){
            widgetFactory.createLabel(row,	condition.getOperand1(), SWT.NONE) ;
            widgetFactory.createLabel(row, condition.getOperator(), SWT.NONE) ;
            widgetFactory.createLabel(row, condition.getOperand2(), SWT.NONE) ;
            if(conditions.indexOf(condition) != conditions.size()-1){
                widgetFactory.createLabel(row, " ", SWT.NONE) ;
                widgetFactory.createLabel(row, Messages.and, SWT.NONE) ;
                widgetFactory.createLabel(row, " ", SWT.NONE) ;
            }
        }

        widgetFactory.createLabel(row,"  ",SWT.NONE) ;
        final Label image = widgetFactory.createLabel(row, "", SWT.NONE) ;
        image.setImage(Pics.getImage(PicsConstants.arrowRight)) ;
        if(decisionTableAction instanceof TakeTransitionAction){
            if(((TakeTransitionAction) decisionTableAction).isTakeTransition()){
                widgetFactory.createLabel(row,"  ",SWT.NONE) ;
                widgetFactory.createLabel(row, Messages.takeTransition,SWT.NONE) ;
            }else{
                widgetFactory.createLabel(row,"  ",SWT.NONE) ;
                widgetFactory.createLabel(row, Messages.dontTakeTransition,SWT.NONE) ;
            }
        }

    }


    /*
     * (non-Javadoc)
     * 
     * @seeorg.bonitasoft.studio.properties.sections.general.
     * IExtenstibleGridPropertySectionContribution#getLabel()
     */
    @Override
    public String getLabel() {
        return Messages.connectionConnection;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.bonitasoft.studio.properties.sections.general.
     * IExtenstibleGridPropertySectionContribution
     * #isRelevantFor(org.eclipse.emf.ecore.EObject)
     */
    @Override
    public boolean isRelevantFor(EObject eObject) {
        if(eObject instanceof SequenceFlow && BonitaProfilesManager.getInstance().isEnabled(IBonitaActivitiesCategory.DATA_MANAGEMENT)){
    		SourceElement source =  ((SequenceFlow)eObject).getSource() ;
    		boolean sourceIdAndGateway = source instanceof ANDGateway ;
    		TargetElement target =  ((SequenceFlow)eObject).getTarget() ;
    		boolean targetIsAndGateway = target instanceof ANDGateway ;

    		return !(eObject instanceof BoundaryEvent) && !sourceIdAndGateway && !targetIsAndGateway;

    	}
    	return false ;

    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.bonitasoft.studio.properties.sections.general.
     * IExtenstibleGridPropertySectionContribution#refresh()
     */
    @Override
    public void refresh() {
        if(transition != null && !conditionSection.isDisposed()){
            refreshBindings() ;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.bonitasoft.studio.properties.sections.general.
     * IExtenstibleGridPropertySectionContribution
     * #setEObject(org.eclipse.emf.ecore.EObject)
     */
    @Override
    public void setEObject(EObject object) {
        transition = (SequenceFlow) object;
    }

    private void refreshBindings() {
        if(conditionSection != null && !conditionSection.isDisposed()){
            if (dataBindingContext != null) {
                dataBindingContext.dispose();
            }

            IConverter conditionConverter = new Converter(Boolean.class,Boolean.class) {

                @Override
                public Object convert(Object fromObject) {
                    if(transition != null && transition.getSource() instanceof CatchLinkEvent){
                        return false ;
                    }

                    return !(Boolean)fromObject;
                }
            };

            IConverter defaultConverter = new Converter(Boolean.class,Boolean.class) {

                @Override
                public Object convert(Object fromObject) {
                    if(transition != null && transition.getSource() instanceof CatchLinkEvent){
                        return false ;
                    }else{
                        SourceElement elem = transition.getSource() ;
                        boolean alreadyExistDefault = false ;
                        for(Connection c  :elem.getOutgoing()){
                            if(c instanceof SequenceFlow ){
                                if(((SequenceFlow) c).isIsDefault() && !c.equals(transition)){
                                    alreadyExistDefault = true ;
                                }
                            }
                        }
                        if(alreadyExistDefault){
                            return false ;
                        }else{
                            return true ;
                        }
                    }

                }
            };

            final UpdateValueStrategy useExpressionTargetToModel = new UpdateValueStrategy() ;
            useExpressionTargetToModel.setConverter(new Converter(Boolean.class,SequenceFlowConditionType.class){

                @Override
                public Object convert(Object fromObject) {
                    if((Boolean)fromObject){
                        return SequenceFlowConditionType.EXPRESSION ;
                    }else{
                        return SequenceFlowConditionType.DECISION_TABLE;
                    }
                }
            });

            final UpdateValueStrategy useDescisionTargetToModel = new UpdateValueStrategy() ;
            useDescisionTargetToModel.setConverter(new Converter(Boolean.class,SequenceFlowConditionType.class){

                @Override
                public Object convert(Object fromObject) {
                    if((Boolean)fromObject){
                        return SequenceFlowConditionType.DECISION_TABLE ;
                    }else{
                        return SequenceFlowConditionType.EXPRESSION;
                    }
                }
            });

            final UpdateValueStrategy useExpressionModelToTarget = new UpdateValueStrategy() ;
            useExpressionModelToTarget.setConverter(new Converter(SequenceFlowConditionType.class,Boolean.class){

                @Override
                public Object convert(Object fromObject) {
                    if(fromObject == SequenceFlowConditionType.EXPRESSION){
                        return true ;
                    }else{
                        return false  ;
                    }
                }
            });

            final UpdateValueStrategy useDecisionModelToTarget = new UpdateValueStrategy() ;
            useDecisionModelToTarget.setConverter(new Converter(SequenceFlowConditionType.class,Boolean.class){

                @Override
                public Object convert(Object fromObject) {
                    if(fromObject == SequenceFlowConditionType.DECISION_TABLE){
                        return true ;
                    }else{
                        return false  ;
                    }
                }
            });


            dataBindingContext = new EMFDataBindingContext();

            IObservableValue typeValue = EMFEditObservables.observeValue(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__CONDITION_TYPE);
            typeValue.addValueChangeListener(new IValueChangeListener() {

                @Override
                public void handleValueChange(ValueChangeEvent event) {
                    if(conditionSection != null && !conditionSection.isDisposed()){
                        if(event.diff.getNewValue() == SequenceFlowConditionType.EXPRESSION && (conditionViewer == null || conditionViewer.getControl().isDisposed())){
                            if(conditionSection.getClient() != null){
                                conditionSection.getClient().dispose() ;
                            }
                            conditionSection.setClient(createExpressionComposite(conditionSection, widgetFactory)) ;
                            conditionSection.setExpanded(true) ;
                        }else if(event.diff.getNewValue() == SequenceFlowConditionType.DECISION_TABLE &&  (conditionViewer != null && !conditionViewer.getControl().isDisposed())){
                            if(conditionSection.getClient() != null){
                                conditionSection.getClient().dispose() ;
                            }
                            conditionSection.setClient(createDecisionTableComposite(conditionSection, widgetFactory)) ;
                            conditionSection.setExpanded(true) ;
                        }
                        refreshBindings() ;
                    }
                }
            }) ;

            dataBindingContext.bindValue(SWTObservables.observeSelection(useExpressionCondition),
                    typeValue,useExpressionTargetToModel,useExpressionModelToTarget);
            dataBindingContext.bindValue(SWTObservables.observeSelection(useDecisionTable),
                    typeValue,useDescisionTargetToModel,useDecisionModelToTarget);


            dataBindingContext.bindValue(SWTObservables.observeSelection(defaultCheckBox),
                    EMFEditObservables.observeValue(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__IS_DEFAULT));

            dataBindingContext.bindValue(SWTObservables.observeEnabled(defaultCheckBox),
                    EMFEditObservables.observeValue(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__IS_DEFAULT),new UpdateValueStrategy().setConverter(defaultConverter),new UpdateValueStrategy().setConverter(defaultConverter));

            if(conditionViewer != null && !conditionViewer.getControl().isDisposed()){
                dataBindingContext.bindValue(SWTObservables.observeEnabled(conditionViewer.getTextControl()),
                        EMFEditObservables.observeValue(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__IS_DEFAULT),new UpdateValueStrategy().setConverter(conditionConverter),new UpdateValueStrategy().setConverter(conditionConverter));
                dataBindingContext.bindValue(SWTObservables.observeEnabled(conditionViewer.getButtonControl()),
                        EMFEditObservables.observeValue(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__IS_DEFAULT),new UpdateValueStrategy().setConverter(conditionConverter),new UpdateValueStrategy().setConverter(conditionConverter));
                conditionViewer.setEditingDomain(editingDomain) ;
                dataBindingContext.bindValue(
                        ViewerProperties.singleSelection().observe(conditionViewer),
                        EMFEditProperties.value(editingDomain, ProcessPackage.Literals.SEQUENCE_FLOW__CONDITION).observe(transition));
                conditionViewer.setInput(transition) ;
            }



            //		conditionText.setEditingDomain(editingDomain);
            //		if (transition.getConditionType() == SequenceFlowConditionType.EXPRESSION && transition.getCondition() != null && transition.getCondition().getContent() != null) {
            //			conditionText.setText(transition.getCondition());
            //		} else if (transition.getConditionType() == SequenceFlowConditionType.DECISION_TABLE) {
            //			conditionText.setText(DecisionTableUtil.DECISION_TABLE);
            //		}
            //		conditionText.addValueChangedListener(new Listener() {
            //			public void handleEvent(Event event) {
            //				if (!DecisionTableUtil.DECISION_TABLE.equals(conditionText.getText())) {
            //					CompoundCommand command = new CompoundCommand();
            //					command.append(new SetCommand(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__CONDITION, conditionText.getText()));
            //					if (transition.getConditionType() == SequenceFlowConditionType.DECISION_TABLE) {
            //						command.append(new SetCommand(editingDomain, transition, ProcessPackage.Literals.SEQUENCE_FLOW__CONDITION_TYPE, SequenceFlowConditionType.GROOVY_EXPRESSION));
            //					}
            //					editingDomain.getCommandStack().execute(command);
            //				}
            //			}
            //
            //		});


        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.bonitasoft.studio.properties.sections.general.
     * IExtenstibleGridPropertySectionContribution
     * #setEditingDomain(org.eclipse.emf.transaction.TransactionalEditingDomain)
     */
    @Override
    public void setEditingDomain(TransactionalEditingDomain editingDomain) {
        this.editingDomain = editingDomain;
        if (conditionViewer != null) {
            conditionViewer.setEditingDomain(editingDomain);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.bonitasoft.studio.properties.sections.general.
     * IExtenstibleGridPropertySectionContribution
     * #setSelection(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void setSelection(ISelection selection) {

    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.properties.IExtensibleGridPropertySectionContribution#dispose()
     */
    @Override
    public void dispose() {
        if(color != null && !color.isDisposed()){
            color.dispose();
        }
        if (dataBindingContext != null) {
            dataBindingContext.dispose();
        }
    }

}
