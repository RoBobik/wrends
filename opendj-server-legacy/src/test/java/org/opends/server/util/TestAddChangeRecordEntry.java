/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2006-2008 Sun Microsystems, Inc.
 * Portions Copyright 2014-2016 ForgeRock AS.
 */
package org.opends.server.util;

import static org.opends.server.util.CollectionUtils.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opends.server.TestCaseUtils;
import org.opends.server.types.Attribute;
import org.forgerock.opendj.ldap.schema.AttributeType;
import org.opends.server.types.Attributes;
import org.forgerock.opendj.ldap.DN;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class defines a set of tests for the
 * {@link org.opends.server.util.AddChangeRecordEntry} class.
 * <p>
 * Note that we test shared behaviour with the abstract
 * {@link org.opends.server.util.ChangeRecordEntry} class in case it has
 * been overridden.
 */
public final class TestAddChangeRecordEntry extends UtilTestCase {
  /** Set of attributes. */
  private Map<AttributeType, List<Attribute>> attributes;

  /** The attribute being added. */
  private Attribute attribute;

  /**
   * Once-only initialization.
   *
   * @throws Exception
   *           If an unexpected error occurred.
   */
  @BeforeClass
  public void setUp() throws Exception {
    // This test suite depends on having the schema available, so we'll
    // start the server.
    TestCaseUtils.startServer();

    attribute = Attributes.create("cn", "hello world");
    attributes = new HashMap<>();
    attributes.put(attribute.getAttributeDescription().getAttributeType(), newArrayList(attribute));
  }

  /**
   * Tests the constructor with null DN.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(expectedExceptions = { NullPointerException.class,
                               AssertionError.class })
  public void testConstructorNullDN() throws Exception {
    new AddChangeRecordEntry(null, attributes);
  }

  /**
   * Tests the constructor with empty DN.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testConstructorEmptyDN() throws Exception {
    AddChangeRecordEntry entry = new AddChangeRecordEntry(DN.rootDN(),
        attributes);

    TestCaseUtils.assertObjectEquals(entry.getDN(), DN.rootDN());
  }

  /**
   * Tests the constructor with non-null DN.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testConstructorNonNullDN() throws Exception {
    DN testDN1 = DN.valueOf("dc=hello, dc=world");
    DN testDN2 = DN.valueOf("dc=hello, dc=world");

    AddChangeRecordEntry entry = new AddChangeRecordEntry(testDN1,
        attributes);

    TestCaseUtils.assertObjectEquals(entry.getDN(), testDN2);
  }

  /**
   * Tests the change operation type is correct.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testChangeOperationType() throws Exception {
    AddChangeRecordEntry entry = new AddChangeRecordEntry(DN.rootDN(), attributes);

    Assert.assertEquals(entry.getChangeOperationType(),
        ChangeOperationType.ADD);
  }

  /**
   * Tests getAttributes method for empty modifications.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testGetAttributesEmpty() throws Exception {
    Map<AttributeType, List<Attribute>> empty = Collections.emptyMap();
    AddChangeRecordEntry entry = new AddChangeRecordEntry(DN.rootDN(), empty);

    List<Attribute> attrs = entry.getAttributes();
    Assert.assertEquals(attrs.size(), 0);
  }

  /**
   * Tests getAttributes method for non-empty modifications.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testGetAttributesNonEmpty() throws Exception {
    AddChangeRecordEntry entry = new AddChangeRecordEntry(DN.rootDN(), attributes);

    List<Attribute> attrs = entry.getAttributes();
    Assert.assertEquals(attrs.size(), 1);

    Attribute first = attrs.get(0);
    TestCaseUtils.assertObjectEquals(first, attribute);
  }
}
