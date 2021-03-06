/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.repository.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseHandlerTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void should_create_bitronix_resource_properties() throws Exception {
        final IProject project = mock(IProject.class, RETURNS_DEEP_STUBS);
        final File rootFolder = tmpFolder.newFolder();
        when(project.getLocation().toFile()).thenReturn(rootFolder);
        final DatabaseHandler databaseHandler = spy(new DatabaseHandler(project));
        final Properties dbProperties = new Properties();
        dbProperties.put(DatabaseHandler.BONITA_DB_NAME_PROPERTY, "bonita_journal.db");
        dbProperties.put(DatabaseHandler.BUSINESS_DATA_DB_NAME_PROPERTY, "business_data.db");
        doReturn(dbProperties).when(databaseHandler).readDatabaseProperties();

        final Path file = databaseHandler.createBitronixConfFile();

        final Properties properties = new Properties();

        try (InputStream is = java.nio.file.Files.newInputStream(file)) {
            properties.load(is);
        }
        assertThat(properties).contains(
                entry("resource.ds1.driverProperties.URL",
                        String.format(
                                "jdbc:h2:file:%s" + File.separator
                                        + "h2_database" + File.separator
                                        + "bonita_journal.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;",
                                rootFolder.getAbsolutePath())),
                entry("resource.ds2.driverProperties.URL",
                        String.format(
                                "jdbc:h2:file:%s" + File.separator
                                        + "h2_database" + File.separator
                                        + "business_data.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;",
                                rootFolder.getAbsolutePath())),
                entry("allowLocalTransactions", "true"),
                entry("resource.ds1.uniqueName", "jdbc/bonitaDSXA"),
                entry("resource.ds2.uniqueName", "jdbc/BusinessDataDSXA"));
    }

}
