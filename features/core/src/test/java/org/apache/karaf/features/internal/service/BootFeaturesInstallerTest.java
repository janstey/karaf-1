/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.features.internal.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.karaf.features.FeaturesService.Option;
import org.apache.karaf.features.TestBase;
import org.easymock.Capture;
import org.junit.Assert;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class BootFeaturesInstallerTest extends TestBase {

    @Test
    @SuppressWarnings("unchecked")
    public void testParser() {
        BootFeaturesInstaller installer = new BootFeaturesInstaller(null, null, "", "", false);
        Assert.assertEquals(asList(setOf("test1", "test2"), setOf("test3")), installer.parseBootFeatures(" ( test1 , test2 ) , test3 "));
        Assert.assertEquals(asList(setOf("test1", "test2", "test3")), installer.parseBootFeatures(" test1 , test2, test3"));
        Assert.assertEquals(asList(setOf("test1"), setOf("test2"), setOf("test3")), installer.parseBootFeatures("(test1), (test2), test3"));
    }
    
    @Test
    public void testDefaultBootFeatures() throws Exception  {
        FeaturesServiceImpl impl = createMock(FeaturesServiceImpl.class);

        Capture<Set<String>> featuresCapture = newCapture();
        impl.installFeatures(capture(featuresCapture), eq(EnumSet.of(Option.NoFailOnFeatureNotFound)));
        expectLastCall();

        impl.bootDone();
        expectLastCall();

        replay(impl);
        BootFeaturesInstaller bootFeatures = new BootFeaturesInstaller(null, impl, "", "config,standard,region", false);
        bootFeatures.installBootFeatures();
        verify(impl);

        List<String> features = new ArrayList<String>(featuresCapture.getValue());
        Assert.assertEquals("config", features.get(0));
        Assert.assertEquals("standard", features.get(1));
        Assert.assertEquals("region", features.get(2));
    }

    @Test
    public void testStagedBoot() throws Exception  {
        FeaturesServiceImpl impl = createStrictMock(FeaturesServiceImpl.class);

        impl.installFeatures(setOf("transaction"), EnumSet.of(Option.NoFailOnFeatureNotFound));
        expectLastCall();
        impl.installFeatures(setOf("ssh"), EnumSet.of(Option.NoFailOnFeatureNotFound));
        expectLastCall();

        impl.bootDone();
        expectLastCall();

        replay(impl);
        BootFeaturesInstaller bootFeatures = new BootFeaturesInstaller(null, impl , "", "(transaction), ssh", false);
        bootFeatures.installBootFeatures();
        verify(impl);
    }

    @Test
    public void testStartDoesNotFailWithOneInvalidUri() throws Exception {
        FeaturesServiceImpl impl = createStrictMock(FeaturesServiceImpl.class);
        impl.addRepository(URI.create("mvn:inexistent/features/1.0/xml/features"));
        expectLastCall().andThrow(new IllegalArgumentException());

        impl.bootDone();
        expectLastCall();

        replay(impl);
        BootFeaturesInstaller bootFeatures = new BootFeaturesInstaller(null, impl, "mvn:inexistent/features/1.0/xml/features", "", false);
        bootFeatures.installBootFeatures();
        verify(impl);
    }

}
