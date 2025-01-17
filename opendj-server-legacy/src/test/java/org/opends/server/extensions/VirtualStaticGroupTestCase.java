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
 * Copyright 2008-2010 Sun Microsystems, Inc.
 * Portions Copyright 2012-2016 ForgeRock AS.
 */
package org.opends.server.extensions;

import java.util.LinkedList;
import java.util.List;

import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.ConditionResult;
import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.ModificationType;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.opendj.ldap.SearchScope;
import org.forgerock.opendj.ldap.schema.AttributeType;
import org.opends.server.TestCaseUtils;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.GroupManager;
import org.opends.server.core.ModifyOperation;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.protocols.internal.InternalSearchOperation;
import org.opends.server.protocols.internal.SearchRequest;
import org.opends.server.types.Attribute;
import org.opends.server.types.Attributes;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;
import org.opends.server.types.MemberList;
import org.opends.server.types.Modification;
import org.opends.server.types.SearchFilter;
import org.opends.server.types.VirtualAttributeRule;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.forgerock.opendj.ldap.schema.CoreSchema.*;
import static org.opends.server.protocols.internal.InternalClientConnection.*;
import static org.opends.server.protocols.internal.Requests.*;
import static org.opends.server.util.CollectionUtils.*;
import static org.testng.Assert.*;

/**
 * A set of test cases for the virtual static group implementation and the
 * member virtual attribute provider.
 */
public class VirtualStaticGroupTestCase
       extends ExtensionsTestCase
{
  /**
   * The lines comprising the LDIF test data.
   */
  private static final String[] LDIF_LINES =
  {
    "dn: ou=People,o=test",
    "objectClass: top",
    "objectClass: organizationalUnit",
    "ou: People",
    "",
    "dn: uid=test.1,ou=People,o=test",
    "objectClass: top",
    "objectClass: person",
    "objectClass: organizationalPerson",
    "objectClass: inetOrgPerson",
    "uid: test.1",
    "givenName: Test",
    "sn: 1",
    "cn: Test 1",
    "userPassword: password",
    "",
    "dn: uid=test.2,ou=People,o=test",
    "objectClass: top",
    "objectClass: person",
    "objectClass: organizationalPerson",
    "objectClass: inetOrgPerson",
    "uid: test.2",
    "givenName: Test",
    "sn: 2",
    "cn: Test 2",
    "userPassword: password",
    "",
    "dn: uid=test.3,ou=People,o=test",
    "objectClass: top",
    "objectClass: person",
    "objectClass: organizationalPerson",
    "objectClass: inetOrgPerson",
    "uid: test.3",
    "givenName: Test",
    "sn: 3",
    "cn: Test 3",
    "userPassword: password",
    "",
    "dn: uid=test.4,ou=People,o=test",
    "objectClass: top",
    "objectClass: person",
    "objectClass: organizationalPerson",
    "objectClass: inetOrgPerson",
    "uid: test.4",
    "givenName: Test",
    "sn: 4",
    "cn: Test 4",
    "userPassword: password",
    "",
    "dn: ou=Groups,o=test",
    "objectClass: top",
    "objectClass: organizationalUnit",
    "ou: Groups",
    "",
    "dn: cn=Dynamic All Users,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfURLs",
    "cn: Dynamic All Users",
    "memberURL: ldap:///ou=People,o=test??sub?(objectClass=person)",
    "",
    "dn: cn=Dynamic One User,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfURLs",
    "cn: Dynamic One User",
    "memberURL: ldap:///ou=People,o=test??sub?(&(objectClass=person)(sn=4))",
    "",
    "dn: cn=Static member List,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfNames",
    "cn: Static member List",
    "member: uid=test.1,ou=People,o=test",
    "member: uid=test.3,ou=People,o=test",
    "",
    "dn: cn=Static uniqueMember List,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfUniqueNames",
    "cn: Static uniqueMember List",
    "uniqueMember: uid=test.2,ou=People,o=test",
    "uniqueMember: uid=test.3,ou=People,o=test",
    "uniqueMember: uid=no-such-user,ou=People,o=test",
    "",
    "dn: cn=Virtual member All Users,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfNames",
    "objectClass: ds-virtual-static-group",
    "cn: Virtual member All Users",
    "ds-target-group-dn: cn=Dynamic All Users,ou=Groups,o=test",
    "",
    "dn: cn=Virtual uniqueMember All Users,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfUniqueNames",
    "objectClass: ds-virtual-static-group",
    "cn: Virtual uniqueMember All Users",
    "ds-target-group-dn: cn=Dynamic All Users,ou=Groups,o=test",
    "",
    "dn: cn=Virtual member One User,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfNames",
    "objectClass: ds-virtual-static-group",
    "cn: Virtual member One User",
    "ds-target-group-dn: cn=Dynamic One User,ou=Groups,o=test",
    "",
    "dn: cn=Virtual uniqueMember One User,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfUniqueNames",
    "objectClass: ds-virtual-static-group",
    "cn: Virtual uniqueMember One User",
    "ds-target-group-dn: cn=Dynamic One User,ou=Groups,o=test",
    "",
    "dn: cn=Virtual Static member List,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfNames",
    "objectClass: ds-virtual-static-group",
    "cn: Virtual Static member List",
    "ds-target-group-dn: cn=Static member List,ou=Groups,o=test",
    "",
    "dn: cn=Virtual Static uniqueMember List,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfUniqueNames",
    "objectClass: ds-virtual-static-group",
    "cn: Virtual Static uniqueMember List",
    "ds-target-group-dn: cn=Static uniqueMember List,ou=Groups,o=test",
    "",
    "dn: cn=Crossover member Static Group,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfUniqueNames",
    "objectClass: ds-virtual-static-group",
    "cn: Crossover member Static Group",
    "ds-target-group-dn: cn=Static member List,ou=Groups,o=test",
    "",
    "dn: cn=Crossover uniqueMember Static Group,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfNames",
    "objectClass: ds-virtual-static-group",
    "cn: Crossover uniqueMember Static Group",
    "ds-target-group-dn: cn=Static uniqueMember List,ou=Groups,o=test",
    "",
    "dn: cn=Virtual Nonexistent,ou=Groups,o=test",
    "objectClass: top",
    "objectClass: groupOfNames",
    "objectClass: ds-virtual-static-group",
    "cn: Virtual Nonexistent",
    "ds-target-group-dn: cn=Nonexistent,ou=Groups,o=test"
  };



  /** The attribute type for the member attribute. */
  private AttributeType memberType;

  /** The attribute type for the uniqueMember attribute. */
  private AttributeType uniqueMemberType;

  /** The server group manager. */
  private GroupManager groupManager;

  /** The DNs of the various entries in the data set. */
  private DN u1;
  private DN u2;
  private DN u3;
  private DN u4;
  private DN da;
  private DN d1;
  private DN sm;
  private DN su;
  private DN vmda;
  private DN vuda;
  private DN vmd1;
  private DN vud1;
  private DN vsm;
  private DN vsu;
  private DN vcm;
  private DN vcu;
  private DN vn;
  private DN ne;



  /**
   * Ensures that the Directory Server is running.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @BeforeClass
  public void startServer()
         throws Exception
  {
    TestCaseUtils.startServer();

    memberType = getMemberAttributeType();
    uniqueMemberType = getUniqueMemberAttributeType();
    groupManager = DirectoryServer.getGroupManager();

    u1 = DN.valueOf("uid=test.1,ou=People,o=test");
    u2 = DN.valueOf("uid=test.2,ou=People,o=test");
    u3 = DN.valueOf("uid=test.3,ou=People,o=test");
    u4 = DN.valueOf("uid=test.4,ou=People,o=test");
    da = DN.valueOf("cn=Dynamic All Users,ou=Groups,o=test");
    d1 = DN.valueOf("cn=Dynamic One User,ou=Groups,o=test");
    sm = DN.valueOf("cn=Static member List,ou=Groups,o=test");
    su = DN.valueOf("cn=Static uniqueMember List,ou=Groups,o=test");
    vmda = DN.valueOf("cn=Virtual member All Users,ou=Groups,o=test");
    vuda = DN.valueOf("cn=Virtual uniqueMember All Users,ou=Groups,o=test");
    vmd1 = DN.valueOf("cn=Virtual member One User,ou=Groups,o=test");
    vud1 = DN.valueOf("cn=Virtual uniqueMember One User,ou=Groups,o=test");
    vsm = DN.valueOf("cn=Virtual Static member List,ou=Groups,o=test");
    vsu = DN.valueOf("cn=Virtual Static uniqueMember List,ou=Groups,o=test");
    vcm = DN.valueOf("cn=Crossover member Static Group,ou=Groups,o=test");
    vcu = DN.valueOf("cn=Crossover uniqueMember Static Group,ou=Groups,o=test");
    vn = DN.valueOf("cn=Virtual Nonexistent,ou=Groups,o=test");
    ne = DN.valueOf("cn=Nonexistent,ou=Groups,o=test");
  }



  /**
   * Tests creating a new instance of a virtual static group from a valid entry.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testCreateValidGroup()
         throws Exception
  {
    Entry entry = TestCaseUtils.makeEntry(
      "dn: cn=Valid Virtual Static Group,ou=Groups,o=test",
      "objectClass: top",
      "objectClass: groupOfNames",
      "objectClass: ds-virtual-static-group",
      "cn: Valid Virtual Static Group",
      "ds-target-group-dn: cn=Static member List,ou=Groups,o=test");

    VirtualStaticGroup groupImplementation = new VirtualStaticGroup();
    VirtualStaticGroup groupInstance = groupImplementation.newInstance(null, entry);
    assertNotNull(groupInstance);
    groupImplementation.finalizeGroupImplementation();
  }



  /**
   * Retrieves a set of invalid vittual static group definition entries.
   *
   * @return  A set of invalid virtul static group definition entries.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @DataProvider(name = "invalidGroups")
  public Object[][] getInvalidGroupDefinitions()
         throws Exception
  {
    List<Entry> groupEntries = TestCaseUtils.makeEntries(
      "dn: cn=Not a Virtual Static Group,ou=Groups,o=test",
      "objectClass: top",
      "objectClass: groupOfNames",
      "cn: Not a Virtual Static Group",
      "member: uid=test.1,ou=People,o=test",
      "",
      "dn: cn=No Target,ou=Groups,o=test",
      "objectClass: top",
      "objectClass: groupOfNames",
      "objectClass: ds-virtual-static-group",
      "cn: No Target",
      "",
      "dn: cn=Invalid Target,ou=Groups,o=test",
      "objectClass: top",
      "objectClass: groupOfNames",
      "objectClass: ds-virtual-static-group",
      "cn: Invalid Target",
      "ds-target-group-dn: invalid");

    Object[][] entryArray = new Object[groupEntries.size()][1];
    for (int i=0; i < entryArray.length; i++)
    {
      entryArray[i][0] = groupEntries.get(i);
    }

    return entryArray;
  }



  /**
   * Tests creating a new instance of a virtual static group from an invalid
   * entry.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test(dataProvider = "invalidGroups",
        expectedExceptions = { DirectoryException.class })
  public void testCreateInvalidGroup(Entry entry)
         throws Exception
  {
    VirtualStaticGroup groupImplementation = new VirtualStaticGroup();
    try
    {
      groupImplementation.newInstance(null, entry);
    }
    finally
    {
      groupImplementation.finalizeGroupImplementation();
    }
  }



  /**
   * Performs general tests of the group API for virtual static groups with a
   * group that has a real target group.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testGroupAPI()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    VirtualStaticGroup g =
         (VirtualStaticGroup) groupManager.getGroupInstance(vmda);
    assertNotNull(g);
    assertTrue(g.isMember(u1));

    assertNotNull(g.getGroupDefinitionFilter());
    TestCaseUtils.assertObjectEquals(g.getGroupDN(), vmda);
    TestCaseUtils.assertObjectEquals(g.getTargetGroupDN(), da);
    assertFalse(g.supportsNestedGroups());
    assertTrue(g.getNestedGroupDNs().isEmpty());
    assertFalse(g.mayAlterMemberList());

    Entry entry = DirectoryServer.getEntry(u1);
    assertTrue(g.isMember(entry));

    MemberList memberList = g.getMembers();
    assertTrue(memberList.hasMoreMembers());
    assertNotNull(memberList.nextMemberDN());
    assertNotNull(memberList.nextMemberEntry());
    assertNotNull(memberList.nextMemberDN());
    assertNotNull(memberList.nextMemberDN());
    assertFalse(memberList.hasMoreMembers());

    SearchFilter filter = SearchFilter.createFilterFromString("(sn=1)");
    memberList = g.getMembers(DN.valueOf("o=test"), SearchScope.WHOLE_SUBTREE,
                              filter);
    assertTrue(memberList.hasMoreMembers());
    assertNotNull(memberList.nextMemberDN());
    assertFalse(memberList.hasMoreMembers());

    try
    {
      g.addNestedGroup(d1);
      fail("Expected an exception from addNestedGroupDN");
    } catch (Exception e) {}

    try
    {
      g.removeNestedGroup(d1);
      fail("Expected an exception from removeNestedGroupDN");
    } catch (Exception e) {}

    try
    {
      g.addMember(entry);
      fail("Expected an exception from addMember");
    } catch (Exception e) {}

    try
    {
      g.removeMember(u1);
      fail("Expected an exception from removeMember");
    } catch (Exception e) {}

    assertNotNull(g.toString());

    cleanUp();
  }



  /**
   * Performs general tests of the group API for virtual static groups with a
   * group that has a nonexistent target group.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testGroupAPINonexistent()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    VirtualStaticGroup g =
         (VirtualStaticGroup) groupManager.getGroupInstance(vn);
    assertNotNull(g);

    assertNotNull(g.getGroupDefinitionFilter());
    TestCaseUtils.assertObjectEquals(g.getGroupDN(), vn);
    TestCaseUtils.assertObjectEquals(g.getTargetGroupDN(), ne);
    assertFalse(g.supportsNestedGroups());
    assertTrue(g.getNestedGroupDNs().isEmpty());
    assertFalse(g.mayAlterMemberList());

    Entry entry = DirectoryServer.getEntry(u1);

    try
    {
      g.isMember(u1);
      fail("Expected an exception from isMember(DN)");
    } catch (Exception e) {}

    try
    {
      g.isMember(entry);
      fail("Expected an exception from isMember(Entry)");
    } catch (Exception e) {}

    try
    {
      g.getMembers();
      fail("Expected an exception from getMembers()");
    } catch (Exception e) {}

    try
    {
      SearchFilter filter = SearchFilter.createFilterFromString("(sn=1)");
      g.getMembers(DN.valueOf("o=test"), SearchScope.WHOLE_SUBTREE, filter);
      fail("Expected an exception from getMembers(base, scope, filter)");
    } catch (Exception e) {}

    try
    {
      g.addNestedGroup(d1);
      fail("Expected an exception from addNestedGroupDN");
    } catch (Exception e) {}

    try
    {
      g.removeNestedGroup(d1);
      fail("Expected an exception from removeNestedGroupDN");
    } catch (Exception e) {}

    try
    {
      g.addMember(entry);
      fail("Expected an exception from addMember");
    } catch (Exception e) {}

    try
    {
      g.removeMember(u1);
      fail("Expected an exception from removeMember");
    } catch (Exception e) {}

    assertNotNull(g.toString());

    cleanUp();
  }



  /**
   * Tests the behavior of the virtual static group with a dynamic group.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testVirtualGroupDynamicGroupWithMember()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    VirtualStaticGroup g =
         (VirtualStaticGroup) groupManager.getGroupInstance(vmda);
    assertNotNull(g);
    assertTrue(g.isMember(u1));
    assertTrue(g.isMember(u2));
    assertTrue(g.isMember(u3));
    assertTrue(g.isMember(u4));

    cleanUp();
  }



  /**
   * Tests the behavior of the virtual static group with a static group based on
   * the member attribute.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testVirtualGroupStaticGroupWithMember()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    VirtualStaticGroup g =
         (VirtualStaticGroup) groupManager.getGroupInstance(vsm);
    assertNotNull(g);
    assertTrue(g.isMember(u1));
    assertFalse(g.isMember(u2));
    assertTrue(g.isMember(u3));
    assertFalse(g.isMember(u4));

    cleanUp();
  }



  /**
   * Tests the behavior of the virtual static group with a static group based on
   * the uniqueMember attribute.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testVirtualGroupStaticGroupWithUniqueMember()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    VirtualStaticGroup g =
         (VirtualStaticGroup) groupManager.getGroupInstance(vsu);
    assertNotNull(g);
    assertFalse(g.isMember(u1));
    assertTrue(g.isMember(u2));
    assertTrue(g.isMember(u3));
    assertFalse(g.isMember(u4));

    cleanUp();
  }



  /**
   * Performs general tests of the virtual attribute provider API for the member
   * virtual attribute with a target group that exists.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testVirtualAttributeAPI()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    VirtualAttributeRule rule = getRule();
    assertNotNull(rule);

    MemberVirtualAttributeProvider provider =
         (MemberVirtualAttributeProvider) rule.getProvider();

    assertNotNull(provider);

    Entry entry = DirectoryServer.getEntry(vsm);
    assertNotNull(entry);

    assertTrue(provider.isMultiValued());

    Attribute values = provider.getValues(entry, rule);
    assertNotNull(values);
    assertFalse(values.isEmpty());
    assertTrue(provider.hasValue(entry, rule));
    assertTrue(provider.hasValue(entry, rule, ByteString.valueOfUtf8(u1.toString())));
    assertFalse(provider.hasValue(entry, rule, ByteString.valueOfUtf8(ne.toString())));
    assertEquals(provider.matchesSubstring(entry, rule, null, null, null),
                 ConditionResult.UNDEFINED);
    assertEquals(provider.greaterThanOrEqualTo(entry, rule, null),
                 ConditionResult.UNDEFINED);
    assertEquals(provider.lessThanOrEqualTo(entry, rule, null),
                 ConditionResult.UNDEFINED);
    assertEquals(provider.approximatelyEqualTo(entry, rule, null),
                 ConditionResult.UNDEFINED);

    SearchFilter filter = SearchFilter.createFilterFromString("(member=" + u1 + ")");
    SearchRequest request = newSearchRequest(DN.valueOf("o=test"), SearchScope.WHOLE_SUBTREE, filter);
    InternalSearchOperation searchOperation =
        new InternalSearchOperation(getRootConnection(), nextOperationID(), nextMessageID(), request);
    assertFalse(provider.isSearchable(rule, searchOperation, false));
    assertFalse(provider.isSearchable(rule, searchOperation, true));

    provider.processSearch(rule, searchOperation);
    assertNotSame(searchOperation.getResultCode(), ResultCode.SUCCESS);

    cleanUp();
  }



  /**
   * Performs general tests of the virtual attribute provider API for the member
   * virtual attribute with a target group that does not exist.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testVirtualAttributeAPINonexistent()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    VirtualAttributeRule rule = getRule();
    assertNotNull(rule);

    MemberVirtualAttributeProvider provider =
         (MemberVirtualAttributeProvider) rule.getProvider();

    assertNotNull(provider);

    Entry entry = DirectoryServer.getEntry(vn);
    assertNotNull(entry);

    assertTrue(provider.isMultiValued());

    Attribute values = provider.getValues(entry, rule);
    assertNotNull(values);
    assertTrue(values.isEmpty());
    assertFalse(provider.hasValue(entry, rule));
    assertFalse(provider.hasValue(entry, rule, ByteString.valueOfUtf8(u1.toString())));
    assertFalse(provider.hasValue(entry, rule, ByteString.valueOfUtf8(ne.toString())));
    assertEquals(provider.matchesSubstring(entry, rule, null, null, null),
                 ConditionResult.UNDEFINED);
    assertEquals(provider.greaterThanOrEqualTo(entry, rule, null),
                 ConditionResult.UNDEFINED);
    assertEquals(provider.lessThanOrEqualTo(entry, rule, null),
                 ConditionResult.UNDEFINED);
    assertEquals(provider.approximatelyEqualTo(entry, rule, null),
                 ConditionResult.UNDEFINED);

    SearchFilter filter = SearchFilter.createFilterFromString("(member=" + u1 + ")");
    SearchRequest request = newSearchRequest(DN.valueOf("o=test"), SearchScope.WHOLE_SUBTREE, filter);
    InternalSearchOperation searchOperation =
        new InternalSearchOperation(getRootConnection(), nextOperationID(), nextMessageID(), request);
    assertFalse(provider.isSearchable(rule, searchOperation, false));
    assertFalse(provider.isSearchable(rule, searchOperation, false));

    provider.processSearch(rule, searchOperation);
    assertNotSame(searchOperation.getResultCode(), ResultCode.SUCCESS);

    cleanUp();
  }

  private VirtualAttributeRule getRule()
  {
    for (VirtualAttributeRule r : DirectoryServer.getVirtualAttributes())
    {
      if (r.getAttributeType().equals(memberType))
      {
        return r;
      }
    }
    return null;
  }

  /**
   * Tests the behavior of the member virtual attribute with a dynamic group.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testVirtualAttrDynamicGroupWithMember()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    Entry e = DirectoryServer.getEntry(vmda);
    assertNotNull(e);
    assertTrue(e.hasAttribute(memberType));

    Attribute a = e.getAllAttributes(memberType).get(0);
    assertEquals(a.size(), 4);
    assertTrue(a.contains(ByteString.valueOfUtf8(u1.toString())));

    cleanUp();
  }



  /**
   * Tests the behavior of the member virtual attribute with a dynamic group.
   * The target dynamic group will initially have only one memberURL which
   * matches only one user, but will then be updated on the fly to contain a
   * second URL that matches all users.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testVirtualAttrDynamicGroupWithUpdatedMemberURLs()
         throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    Entry e = DirectoryServer.getEntry(vmd1);
    assertNotNull(e);
    assertTrue(e.hasAttribute(memberType));

    Attribute a = e.getAllAttributes(memberType).get(0);
    assertEquals(a.size(), 1);

    ByteString v = ByteString.valueOfUtf8(u4.toString());
    assertTrue(a.contains(v));

    LinkedList<Modification> mods = newLinkedList(new Modification(ModificationType.ADD,
        Attributes.create("memberurl", "ldap:///o=test??sub?(objectClass=person)")));
    ModifyOperation modifyOperation = getRootConnection().processModify(d1, mods);
    assertEquals(modifyOperation.getResultCode(), ResultCode.SUCCESS);

    a = e.getAllAttributes(memberType).get(0);
    assertEquals(a.size(), 4);
    assertTrue(a.contains(v));

    cleanUp();
  }



  /**
   * Tests the behavior of the member virtual attribute with different settings
   * for the "allow retrieving membership" attribute.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test
  public void testAllowRetrievingMembership() throws Exception
  {
    TestCaseUtils.initializeTestBackend(true);
    TestCaseUtils.addEntries(LDIF_LINES);

    Entry e = DirectoryServer.getEntry(vmd1);
    assertNotNull(e);
    assertTrue(e.hasAttribute(memberType));

    Attribute a = e.getAllAttributes(memberType).get(0);
    assertEquals(a.size(), 1);

    ByteString v = ByteString.valueOfUtf8(u4.toString());
    assertTrue(a.contains(v));


    InternalClientConnection conn = getRootConnection();

    LinkedList<Modification> mods = newLinkedList(new Modification(ModificationType.REPLACE,
        Attributes.create("ds-cfg-allow-retrieving-membership", "false")));
    DN definitionDN =
         DN.valueOf("cn=Virtual Static member,cn=Virtual Attributes,cn=config");
    ModifyOperation modifyOperation = conn.processModify(definitionDN, mods);
    assertEquals(modifyOperation.getResultCode(), ResultCode.SUCCESS);


    e = DirectoryServer.getEntry(vmd1);
    assertNotNull(e);
    assertTrue(e.hasAttribute(memberType));

    a = e.getAllAttributes(memberType).get(0);
    assertEquals(a.size(), 0);

    v = ByteString.valueOfUtf8(u4.toString());
    assertTrue(a.contains(v));


    mods = newLinkedList(new Modification(ModificationType.REPLACE,
        Attributes.create("ds-cfg-allow-retrieving-membership", "true")));
    modifyOperation = conn.processModify(definitionDN, mods);
    assertEquals(modifyOperation.getResultCode(), ResultCode.SUCCESS);


    cleanUp();
  }



  /**
   * Removes all of the groups that have been added to the server.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  private void cleanUp() throws Exception
  {
    DN dn = DN.valueOf("ou=Groups,dc=example,dc=com");
    final SearchRequest request = newSearchRequest(dn, SearchScope.SINGLE_LEVEL);
    InternalSearchOperation searchOperation = getRootConnection().processSearch(request);
    for (Entry e : searchOperation.getSearchEntries())
    {
      getRootConnection().processDelete(e.getName());
    }
  }
}

