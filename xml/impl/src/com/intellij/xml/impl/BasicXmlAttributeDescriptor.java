/*
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: Aug 27, 2002
 * Time: 9:55:06 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.xml.impl;

import com.intellij.codeInsight.daemon.XmlErrorMessages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.Nullable;

public abstract class BasicXmlAttributeDescriptor implements XmlAttributeDescriptor {
  public String validateValue(XmlElement context, String value) {
    if (isFixed() && getDefaultValue() != null) {
      String defaultValue = getDefaultValue();

      if (!defaultValue.equals(value)) {
        return XmlErrorMessages.message("attribute.should.have.fixed.value", getName(), defaultValue);
      }
    }

    if (isEnumerated(context) && XmlUtil.isSimpleXmlAttributeValue(value, (XmlAttributeValue)context)) {
      String[] values = getEnumeratedValues(context);
      boolean valueWasFound = false;

      for (int i = 0; i < values.length; i++) {
        String enumValue = values[i];

        if (enumValue.equals(value)) {
          valueWasFound = true;
          break;
        }
      }

      if (!valueWasFound) {
        return XmlErrorMessages.message("wrong.attribute.value");
      }
    }

    return null;
  }

  public String getName(PsiElement context){
    return getName();
  }

  public String[] getEnumeratedValues(@Nullable XmlElement context) {
    return getEnumeratedValues();
  }

  public boolean isEnumerated(@Nullable XmlElement context) {
    return isEnumerated();
  }
}
