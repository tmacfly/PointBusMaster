package com.fly.ui.tool;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 * @Author: wangxiang
 * @CreateDate: 2020/8/17 7:12 PM
 * @Description:
 */
public class CheckTools {
    public static boolean checkHasNoErrors(ExecutableElement element, Messager messager) {
        if (element.getModifiers().contains(Modifier.STATIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must not be static", element);
            return false;
        }

        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber could not be ABSTRACT", element);
            return false;
        }

        if (element.getModifiers().contains(Modifier.NATIVE)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber could not be NATIVE", element);
            return false;
        }

        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must be public", element);
            return false;
        }

        List<? extends VariableElement> parameters = ((ExecutableElement) element).getParameters();
        if (parameters.size() != 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must have exactly 1 parameter", element);
            return false;
        }
        return true;
    }

    public static boolean checkClass(Messager mMessager, String myPackage, TypeElement typeElement) {
        Set<Modifier> modifiers = typeElement.getModifiers();
        if (modifiers.contains(Modifier.PUBLIC)) {
            return true;
        } else if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.ABSTRACT) || modifiers.contains(Modifier.PROTECTED)) {
            return false;
        }
        return false;
    }

    private static PackageElement getPackageElement(TypeElement subscriberClass) {
        Element candidate = subscriberClass.getEnclosingElement();
        while (!(candidate instanceof PackageElement)) {
            candidate = candidate.getEnclosingElement();
        }
        return (PackageElement) candidate;
    }

    public static void print(Messager mMessager, String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

}
