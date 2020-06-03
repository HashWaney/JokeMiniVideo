package org.hash.joke.libnavcompiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;

import org.hash.joke.libnavannotation.ActivityDestination;
import org.hash.joke.libnavannotation.FragmentDestination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Created by Hash on 2020/6/3.
 * App 页面导航信息收集 注解处理器： 什么意思呢---》就是将所有的页面通过路由管理起来，
 * 跳转的时候通过导航路由来进行切换
 * 这里提出一个问题：Fragment的页面路由跳转是如何切换的
 * 通过什么？ FragmentManager show 和 hide 隐藏实现吗？
 * <p>
 * AutoService注解： annotationProcessor project() 编译时，就会自动执行该类
 * </p>
 *
 * <p>
 * SupportedSourceVersion注解：声明支持的jdk版本
 * </p>
 *
 * <p>
 * SupportAnnotationTypes注解：声明该注解处理器想要处理哪些注解
 * </p>
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(
        {"org.hash.joke.libnavannotation.FragmentDestination",
                "org.hash.joke.libnavannotation.ActivityDestination"})
public class NavProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private static final String OUTPUT_FILE_NAME = "destination.json";


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // 日志打印
        messager = processingEnv.getMessager();
        //文件处理工具
        filer = processingEnv.getFiler();

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 通过处理器环境上下文roundEnv分别获取项目中标记为FragmentDestination.class 与 ActivityDestination.class
        // 注解 目的就是为了收集项目中哪些类被注解标记了
        Set<? extends Element> fragmentElements = roundEnv.getElementsAnnotatedWith(FragmentDestination.class);
        Set<? extends Element> activityElements = roundEnv.getElementsAnnotatedWith(ActivityDestination.class);
        if (!fragmentElements.isEmpty() || !activityElements.isEmpty()) {
            HashMap<String, JSONObject> destMap = new HashMap<>();
            // 分别处理FragmentDestination  和 ActivityDestination 注解类型
            // 并收集到destMap这个map中，
            handleDestination(fragmentElements, FragmentDestination.class, destMap);
            handleDestination(activityElements, ActivityDestination.class, destMap);

            // app/src/main/assets 输出到这个目录 立即推出 FileOutputStream
            FileOutputStream fos = null;
            // 将字符流转换为特定编码的字节流
            OutputStreamWriter ops = null;
            try {
                //创建源文件

                /*  intermediates 中间体
                    StandardLocation.CLASS_OUTPUT: java文件生成class文件的位置 /app/build/intermediates/javac/debug/classes/
                    StandardLocation.SOURCE_OUTPUT: java文件的位置，一般在/../app/build/generated/source/apt/
                    StandardLocation.CLASS_PATH: 生成指定文件的的pkg报名
                    StandardLocation.SOURCE_PATH:
                 */
                FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                String resourcePath = fileObject.toUri().getPath();

                // NOTE 其实就是message的Log.i() 级别 但是ERROR 会阻断
                messager.printMessage(Diagnostic.Kind.NOTE, "resourcePath:" + resourcePath);
                // 指定class文件输出的地方
                // app/src/main/assets/
                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);

                //打个日志
                messager.printMessage(Diagnostic.Kind.NOTE, "appPath:" + appPath);

                // 指定assets的路径
                String assetsPath = appPath + "src/main/assets/";

                File file = new File(assetsPath);
                if (!file.exists()) {
                    // 文件夹
                    file.mkdirs();
                }

                // 写入到assets目录中的OUT_PUT_FILE_NAME文件中 其实就是写入到json文件中
                // 比如说我们服务端可以下发json文件到assets目录中， 这样就可以动态的替换资源
                File outPutFile = new File(file, OUTPUT_FILE_NAME);
                if (outPutFile.exists()) {
                    outPutFile.delete();
                }
                outPutFile.createNewFile();

                // 把文件流输出到file中 原始字节流
                fos = new FileOutputStream(outPutFile);
                // 其实这里就是读取注解配置的信息然后收集到map中，通过读取map的数据，
                String content = JSON.toJSONString(destMap);
                // 因为Android文件默认是用UTF-8编码格式 使用Writer可以指定编码格式
                ops = new OutputStreamWriter(fos, "UTF-8");
                ops.write(content);
                ops.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ops != null) {
                    try {
                        ops.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        return true;
    }

    private void handleDestination(Set<? extends Element> elements, Class<? extends Annotation>
            annotationClass, HashMap<String, JSONObject> destMap) {
        for (Element element : elements) {
            // TypeElement 是 类型 （Class ）
            // 如果要解析元素 得到Class的相关信息，就需要强制转换了
            TypeElement typeElement = (TypeElement) element;

            // 全类名之后在干嘛 ？ 就知道他是那个类， 知道了他是那个类，那就好办了
            // 直接对进行操作， 那么用户的操作就生效了，
            // 是否登陆，进行处理
            // 页面路径记录
            // 这里就是相当于将所有页面的信息做了一个统计
            // 把这些信息上报给服务端
            // 拿到注解之后，因为需要的信息是由外界的注解提供的，因此在这里，通过找到类，就找到了类中的注解
            // 也就响应的找到了注解的信息，最后进行信息的统计即可

            //全类名： com.hash.home
            String className = typeElement.getQualifiedName().toString();
            // 页面id 不能重复，即唯一标示
            int id = Math.abs(className.hashCode());
            // 页面的pageUrl 隐士跳转意图中的host://schema//path
            String pageUrl = null;
            //是否需要登陆
            boolean needLogin = false;
            //是否作为第一个页面进行启动
            boolean asStarter = false;
            //页面类型 Activity Fragment
            boolean isFragment = false;

            Annotation annotation = element.getAnnotation(annotationClass);
            if (annotation instanceof FragmentDestination) {
                FragmentDestination fragmentDe = (FragmentDestination) annotation;
                pageUrl = fragmentDe.pageUrl();
                asStarter = fragmentDe.asStarter();
                isFragment = true;
                needLogin = fragmentDe.needLogin();
            } else if (annotation instanceof ActivityDestination) {
                ActivityDestination activityDe = (ActivityDestination) annotation;
                isFragment = false;
                asStarter = activityDe.asStarter();
                needLogin = activityDe.needLogin();
                pageUrl = activityDe.pageUrl();
            }

            // destMap 的键 是唯一的，也即 页面是唯一的键
            if (destMap.containsKey(pageUrl)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "页面路由有误，请重写配置不同的页面路径，不能与之前配置的路径相同，保证页面路径的唯一性");
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", id);
                jsonObject.put("needLogin", needLogin);
                jsonObject.put("asStarter", asStarter);
                jsonObject.put("pageUrl", pageUrl);
                jsonObject.put("className", className);
                jsonObject.put("isFragment", isFragment);
                destMap.put(pageUrl, jsonObject);
            }
        }
    }
}
