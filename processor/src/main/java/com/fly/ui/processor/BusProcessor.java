package com.fly.ui.processor;

import com.fly.annotation.PointSubscribe;
import com.fly.ui.subscriber.SimpleSubscribeInfo;
import com.fly.ui.subscriber.SubscriberFinder;
import com.fly.ui.tool.CheckTools;
import com.google.auto.service.AutoService;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedOptions(value = {"PointBusIndex"})
@SupportedAnnotationTypes("com.fly.annotation.PointSubscribe")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BusProcessor extends AbstractProcessor {
    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;
    private boolean isFst;
    public static final String OPTION_EVENT_BUS_INDEX = "PointBusIndex";
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Messager messager = processingEnv.getMessager();
        String index = processingEnv.getOptions().get(OPTION_EVENT_BUS_INDEX);
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(PointSubscribe.class);
        CheckTools.print(messager, String.format("index = %s", index));
//        print(String.format("bindViewElements size = %s", bindViewElements.size()));
//        String index = processingEnv.getOptions().get(OPTION_EVENT_BUS_INDEX);
//        print(String.format("index = %s", index));
        for (Element element : bindViewElements) {
            PackageElement packageElement = mElementUtils.getPackageOf(element);
            String packName = packageElement.getQualifiedName().toString();
            //CheckTools.print(mMessager, String.format("package = %s", packName));
            if (element instanceof ExecutableElement) {
                PointSubscribe bindView = element.getAnnotation(PointSubscribe.class);
                String threadMode = bindView.threadMode().name();
                //CheckTools.print(mMessager, String.format("threadMode = %s", threadMode));
                ExecutableElement method = (ExecutableElement) element;
                if (CheckTools.checkHasNoErrors(method, messager)) {
                    TypeElement classElement = (TypeElement) method.getEnclosingElement();
                    VariableElement param = method.getParameters().get(0);
                    TypeMirror typeMirror = getParamTypeMirror(param, messager);
                    String className = classElement.getSimpleName().toString();
                    CheckTools.print(mMessager,String.format("className = %s", classElement.getSimpleName().toString()));
//                print(String.format("typeMirror name  = %s", typeMirror.toString()));
                    String fullClassName = packName + "." + className;
                    if (CheckTools.checkClass(mMessager, packName, classElement)) {
                        List<SimpleSubscribeInfo> simpleSubscribeInfoList;
                        if (SubscriberFinder.subscribeInfoHashMap.get(fullClassName) == null) {
                            simpleSubscribeInfoList = new ArrayList<>();
                        } else {
                            simpleSubscribeInfoList = SubscriberFinder.subscribeInfoHashMap.get(fullClassName);
                        }
                        simpleSubscribeInfoList.add(new SimpleSubscribeInfo(method.getSimpleName().toString(), typeMirror.toString(), threadMode));
                        SubscriberFinder.subscribeInfoHashMap.put(fullClassName, simpleSubscribeInfoList);
                    }
                }
            }
        }
        if (!isFst) {
            isFst = true;
            createSubscribeFile(index,"com.fly.subscribe", SubscriberFinder.subscribeInfoHashMap);
        }
        return true;
    }

    private void createSubscribeFile(String index,String packName,HashMap<String, List<SimpleSubscribeInfo>> hashMap) {
        String newClassName = index;
        //String newClassName = index;

        try {
            JavaFileObject jfo = mFiler.createSourceFile(packName + "." + newClassName, new Element[]{});
            Writer writer = jfo.openWriter();
            writer.write("package " + packName + ";");
            writer.write("\n\n");
            writer.write("import com.fly.bus.SubscribeImpl;");
            writer.write("\n");
            writer.write("import com.fly.bus.SubscribeInfo;");
            writer.write("\n");
            writer.write("import java.util.ArrayList;");
            writer.write("\n");
            writer.write("import java.util.HashMap;");
            writer.write("\n");
            writer.write("import java.util.List;");
            writer.write("\n");
            writer.write("public class " + newClassName + " implements SubscribeImpl{\n\n");
            writer.write("      public static HashMap<String,List<SubscribeInfo>> subscribeInfoMap;\n");
            writer.write("      public static List<SubscribeInfo> subscribeInfoList;\n");
            writer.write("      static {\n");
            writer.write("          subscribeInfoMap = new HashMap<>();\n");
            initSubscribeInfo(writer, hashMap);
            writer.write("      }\n");
            writer.write("      public static void initSubscribeInfo(String methodName,String eventName,String threadMode){\n");
            writer.write("          subscribeInfoList.add(new SubscribeInfo(methodName,eventName,threadMode)); \n");
            writer.write("      }\n");
            writer.write("      @Override\n");
            writer.write("      public HashMap<String, List<SubscribeInfo>> getSubscribeInfo(){\n");
            writer.write("              return subscribeInfoMap;\n");
            writer.write("      }\n");
            writer.write("\n\n");
            writer.write("}\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSubscribeInfo(Writer writer, HashMap<String, List<SimpleSubscribeInfo>> hashMap) {
        try {
            for (String className : hashMap.keySet()) {
                writer.write("          subscribeInfoList = new ArrayList<>();\n");
                List<SimpleSubscribeInfo> infoList = hashMap.get(className);
                if (infoList == null || infoList.size() == 0) {
                    continue;
                }
                for (SimpleSubscribeInfo info : infoList) {
                    String methodName = info.getMethodName();
                    String eventName = info.getEventName();
                    String threadMode = info.getThreadMode();
                    writer.write("          initSubscribeInfo(" + "\"" + methodName + "\"" + "," + "\"" + eventName + "\"" + "," + "\"" + threadMode + "\"" + "); \n");
                }
                writer.write("          subscribeInfoMap.put(" + "\"" + className + "\"" + ",subscribeInfoList);\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TypeMirror getParamTypeMirror(VariableElement param, Messager messager) {
        TypeMirror typeMirror = param.asType();
        // Check for generic type
        if (typeMirror instanceof TypeVariable) {
            TypeMirror upperBound = ((TypeVariable) typeMirror).getUpperBound();
            if (upperBound instanceof DeclaredType) {
                if (messager != null) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "Using upper bound type " + upperBound +
                            " for generic parameter", param);
                }
                typeMirror = upperBound;
            }
        }
        return typeMirror;
    }
}