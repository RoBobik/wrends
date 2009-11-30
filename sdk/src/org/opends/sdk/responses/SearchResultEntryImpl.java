/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk.responses;



import java.util.Collection;

import org.opends.sdk.Attribute;
import org.opends.sdk.AttributeDescription;
import org.opends.sdk.DN;
import org.opends.sdk.Entry;
import org.opends.sdk.schema.ObjectClass;
import org.opends.sdk.util.ByteString;
import org.opends.sdk.util.LocalizedIllegalArgumentException;



/**
 * Search result entry implementation.
 */
final class SearchResultEntryImpl extends
    AbstractResponseImpl<SearchResultEntry> implements
    SearchResultEntry
{

  private final Entry entry;



  /**
   * Creates a new search result entry backed by the provided entry.
   * Modifications made to {@code entry} will be reflected in the
   * returned search result entry. The returned search result entry
   * supports updates to its list of controls, as well as updates to the
   * name and attributes if the underlying entry allows.
   *
   * @param entry
   *          The entry.
   * @throws NullPointerException
   *           If {@code entry} was {@code null} .
   */
  public SearchResultEntryImpl(Entry entry) throws NullPointerException
  {
    this.entry = entry;
  }



  /**
   * {@inheritDoc}
   */
  public boolean addAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException
  {
    return entry.addAttribute(attribute);
  }



  /**
   * {@inheritDoc}
   */
  public boolean addAttribute(Attribute attribute,
      Collection<ByteString> duplicateValues)
      throws UnsupportedOperationException, NullPointerException
  {
    return entry.addAttribute(attribute, duplicateValues);
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      Object... values) throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    entry.addAttribute(attributeDescription, values);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry clearAttributes()
      throws UnsupportedOperationException
  {
    entry.clearAttributes();
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsAttribute(
      AttributeDescription attributeDescription)
      throws NullPointerException
  {
    return entry.containsAttribute(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsAttribute(String attributeDescription)
      throws LocalizedIllegalArgumentException, NullPointerException
  {
    return entry.containsAttribute(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsObjectClass(ObjectClass objectClass)
      throws NullPointerException
  {
    return entry.containsObjectClass(objectClass);
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsObjectClass(String objectClass)
      throws NullPointerException
  {
    return entry.containsObjectClass(objectClass);
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<Attribute> findAttributes(
      AttributeDescription attributeDescription)
      throws NullPointerException
  {
    return entry.findAttributes(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<Attribute> findAttributes(String attributeDescription)
      throws LocalizedIllegalArgumentException, NullPointerException
  {
    return entry.findAttributes(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public Attribute getAttribute(
      AttributeDescription attributeDescription)
      throws NullPointerException
  {
    return entry.getAttribute(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public Attribute getAttribute(String attributeDescription)
      throws LocalizedIllegalArgumentException, NullPointerException
  {
    return entry.getAttribute(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public int getAttributeCount()
  {
    return entry.getAttributeCount();
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<Attribute> getAttributes()
  {
    return entry.getAttributes();
  }



  /**
   * {@inheritDoc}
   */
  public DN getName()
  {
    return entry.getName();
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<String> getObjectClasses()
  {
    return entry.getObjectClasses();
  }



  /**
   * {@inheritDoc}
   */
  public boolean removeAttribute(Attribute attribute,
      Collection<ByteString> missingValues)
      throws UnsupportedOperationException, NullPointerException
  {
    return entry.removeAttribute(attribute, missingValues);
  }



  /**
   * {@inheritDoc}
   */
  public boolean removeAttribute(
      AttributeDescription attributeDescription)
      throws UnsupportedOperationException, NullPointerException
  {
    return entry.removeAttribute(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry removeAttribute(String attributeDescription)
      throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    entry.removeAttribute(attributeDescription);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry removeAttribute(String attributeDescription,
      Object... values) throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    entry.removeAttribute(attributeDescription, values);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public boolean replaceAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException
  {
    return entry.replaceAttribute(attribute);
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry replaceAttribute(
      String attributeDescription, Object... values)
      throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    entry.replaceAttribute(attributeDescription, values);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry setName(DN dn)
      throws UnsupportedOperationException, NullPointerException
  {
    entry.setName(dn);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry setName(String dn)
      throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    entry.setName(dn);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SearchResultEntry(name=");
    builder.append(getName());
    builder.append(", attributes=");
    builder.append(getAttributes());
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }



  SearchResultEntry getThis()
  {
    return this;
  }
}
