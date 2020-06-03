package org.hash.joke.libnavannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by Hash on 2020/6/3.
 *
 * @Description: 用来管理生成管理Activity的路由路径
 * @Scop: 当然是作用在类上了，标记这个Activity
 * @method: {pageUrl()----> 页面路径， needLogin()----->是否登陆(业务统一判断，这里只是举例，可能还有其他公共的需求自行添加)
 * asStarter()---->初始页面}
 */
@Target(ElementType.TYPE)
public @interface ActivityDestination {
    String pageUrl();

    boolean needLogin() default false;

    boolean asStarter() default false;
}
