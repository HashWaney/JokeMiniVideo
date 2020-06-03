package org.hash.joke.libnavannotation;

/**
 * Created by Hash on 2020/6/3.
 *
 * @Desription： 用来管理生成Fragment页面的路由路径
 * @Scop： 作用在Type上，标记Fragment
 */
public @interface FragmentDestination {

    String pageUrl();

    boolean needLogin() default false;

    boolean asStarter() default false;
}
