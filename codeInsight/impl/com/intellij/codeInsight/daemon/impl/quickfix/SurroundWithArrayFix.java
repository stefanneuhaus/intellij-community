/*
 * User: anna
 * Date: 21-Mar-2008
 */
package com.intellij.codeInsight.daemon.impl.quickfix;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SurroundWithArrayFix extends PsiElementBaseIntentionAction {
  private final PsiMethodCallExpression myMethodCall;

  public SurroundWithArrayFix(final PsiMethodCallExpression methodCall) {
    myMethodCall = methodCall;
  }

  @NotNull
  public String getText() {
    return "Surround with array initialization";
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, @Nullable final PsiElement element) {
    return myMethodCall != null && myMethodCall.isValid() && getExpression(element) != null;
 }

  @Nullable
  private PsiExpression getExpression(PsiElement element) {
    final PsiElement method = myMethodCall.getMethodExpression().resolve();
    if (method != null) {
      final PsiMethod psiMethod = (PsiMethod)method;
      final PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
      final PsiExpressionList argumentList = myMethodCall.getArgumentList();
      int idx = 0;
      for (PsiExpression expression : argumentList.getExpressions()) {
        if (element != null && PsiTreeUtil.isAncestor(expression, element, false)) {
          if (psiParameters.length > idx) {
            final PsiType paramType = psiParameters[idx].getType();
            if (paramType instanceof PsiArrayType) {
              final PsiType expressionType = expression.getType();
              if (expressionType != null && expressionType.isAssignableFrom(((PsiArrayType)paramType).getComponentType())) {
                return expression;
              }
            }
          }
        }
        idx++;
      }
    }
    return null;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
    final PsiExpression expression = getExpression(file.findElementAt(editor.getCaretModel().getOffset()));
    assert expression != null;
    final PsiExpression toReplace = elementFactory.createExpressionFromText(getArrayCreation(expression), myMethodCall);
    JavaCodeStyleManager.getInstance(project).shortenClassReferences(expression.replace(toReplace));
  }

  @NonNls
  private static String getArrayCreation(@NotNull PsiExpression expression) {
    final PsiType expressionType = expression.getType();
    assert expressionType != null;
    return "new " + expressionType.getCanonicalText() + "[]{" + expression.getText()+ "}";
  }

  public boolean startInWriteAction() {
    return true;
  }
}