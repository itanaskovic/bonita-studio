/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.designer.ui.property.section.control;

import static org.bonitasoft.studio.common.jface.databinding.UpdateStrategyFactory.neverUpdateValueStrategy;
import static org.bonitasoft.studio.common.jface.databinding.UpdateStrategyFactory.updateValueStrategy;

import org.bonitasoft.studio.common.jface.databinding.CustomEMFEditObservables;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.designer.core.expression.CreateNewFormProposalListener;
import org.bonitasoft.studio.designer.i18n.Messages;
import org.bonitasoft.studio.model.process.FormMappingType;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.preferences.BonitaPreferenceConstants;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

/**
 * @author Romain Bioteau
 */
public class FormMappingRadioGroup extends Composite implements BonitaPreferenceConstants {

    private final SelectObservableValue mappingTypeObservable;
    private final InternalMappingComposite pageDesignerMappingComposite;
    private final URLMappingComposite urlMappingComposite;
    private final CustomStackLayout stackLayout;
    private final Composite legacyComposite;

    public FormMappingRadioGroup(final Composite parent, final TabbedPropertySheetWidgetFactory widgetFactory, final IEclipsePreferences preferenceStore,
            final RepositoryAccessor repositoryAccessor, final FormReferenceExpressionValidator formReferenceExpressionValidator,
            final CreateNewFormProposalListener createNewFormListener) {
        super(parent, SWT.NONE);
        setLayout(GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(10, 10, 10, 10).create());

        final Button pageDesignerRadio = widgetFactory.createButton(this, Messages.pageDesigner, SWT.RADIO);
        pageDesignerRadio.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).create());
        final Button externalRadio = widgetFactory.createButton(this, Messages.externalURL, SWT.RADIO);
        externalRadio.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).create());

        final Button legacyRadio = widgetFactory.createButton(this, Messages.legacyForm, SWT.RADIO);
        legacyRadio.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).create());

        mappingTypeObservable = new SelectObservableValue(Boolean.class);
        mappingTypeObservable.addOption(FormMappingType.INTERNAL, SWTObservables.observeSelection(pageDesignerRadio));
        mappingTypeObservable.addOption(FormMappingType.URL, SWTObservables.observeSelection(externalRadio));
        mappingTypeObservable.addOption(FormMappingType.LEGACY, SWTObservables.observeSelection(legacyRadio));

        final Composite stackedComposite = widgetFactory.createComposite(this);
        stackedComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(3, 1).create());

        stackLayout = new CustomStackLayout(stackedComposite);
        stackedComposite.setLayout(stackLayout);

        legacyComposite = widgetFactory.createComposite(stackedComposite);
        pageDesignerMappingComposite = new InternalMappingComposite(stackedComposite, widgetFactory, preferenceStore, repositoryAccessor,
                formReferenceExpressionValidator, createNewFormListener);
        urlMappingComposite = new URLMappingComposite(stackedComposite, widgetFactory);
        widgetFactory.adapt(this);
    }

    private Control compositeFor(final FormMappingType mappingType) {
        switch (mappingType) {
            case URL:
                return urlMappingComposite;
            case INTERNAL:
                return pageDesignerMappingComposite;
            case LEGACY:
            default:
                return legacyComposite;
        }
    }

    public void doBindControl(final DataBindingContext context, final IObservableValue formMappingObservable) {
        context.bindValue(PojoObservables.observeValue(stackLayout, "topControl"), mappingTypeObservable, neverUpdateValueStrategy().create(),
                updateValueStrategy()
                        .withConverter(mappingTypeToCompositeConverter()).create());
        context.bindValue(mappingTypeObservable,
                CustomEMFEditObservables.observeDetailValue(Realm.getDefault(),
                        formMappingObservable,
                        ProcessPackage.Literals.FORM_MAPPING__TYPE));
        pageDesignerMappingComposite.doBindControl(context, formMappingObservable);
        urlMappingComposite.doBindControl(context, formMappingObservable);
    }

    private IConverter mappingTypeToCompositeConverter() {
        return new Converter(Boolean.class, Composite.class) {

            @Override
            public Object convert(final Object mappingType) {
                return compositeFor((FormMappingType) mappingType);
            }
        };
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        if (pageDesignerMappingComposite != null) {
            pageDesignerMappingComposite.dispose();
        }
        if (urlMappingComposite != null) {
            urlMappingComposite.dispose();
        }
        super.dispose();
    }
}
