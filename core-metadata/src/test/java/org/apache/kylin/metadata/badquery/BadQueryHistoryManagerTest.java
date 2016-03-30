/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kylin.metadata.badquery;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.NavigableSet;

import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.util.JsonUtil;
import org.apache.kylin.common.util.LocalFileMetadataTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BadQueryHistoryManagerTest extends LocalFileMetadataTestCase {
    @Before
    public void setUp() throws Exception {
        this.createTestMetadata();
    }

    @After
    public void after() throws Exception {
        this.cleanupTestMetadata();
    }

    @Test
    public void testBasics() throws Exception {
        BadQueryHistory history = BadQueryHistoryManager.getInstance(getTestConfig()).getBadQueriesForProject("default");
        System.out.println(JsonUtil.writeValueAsIndentString(history));

        NavigableSet<BadQueryEntry> entries = history.getEntries();
        assertEquals(2, entries.size());

        BadQueryEntry entry1 = entries.first();
        assertEquals("Slow", entry1.getAdj());
        assertEquals("sandbox.hortonworks.com", entry1.getServer());
        assertEquals("select count(*) from test_kylin_fact", entry1.getSql());

        entries.pollFirst();
        BadQueryEntry entry2 = entries.first();
        assertTrue(entry2.getStartTime() > entry1.getStartTime());
    }

    @Test
    public void testAddEntryToProject() throws IOException {
        KylinConfig kylinConfig = getTestConfig();
        BadQueryHistoryManager manager = BadQueryHistoryManager.getInstance(kylinConfig);
        BadQueryHistory history = manager.addEntryToProject("sql", "adj", 1459362239992L, "server", "t-0", "default");
        NavigableSet<BadQueryEntry> entries = history.getEntries();
        assertEquals(3, entries.size());

        BadQueryEntry newEntry = entries.last();

        System.out.println(newEntry);
        assertEquals("sql", newEntry.getSql());
        assertEquals("adj", newEntry.getAdj());
        assertEquals(1459362239992L, newEntry.getStartTime());
        assertEquals("server", newEntry.getServer());
        assertEquals("t-0", newEntry.getThread());

        for (int i = 0; i < kylinConfig.getBadQueryHistoryNum(); i++) {
            history = manager.addEntryToProject("sql", "adj", 1459362239993L + i, "server", "t-0", "default");
        }
        assertEquals(kylinConfig.getBadQueryHistoryNum(), history.getEntries().size());
    }

}
