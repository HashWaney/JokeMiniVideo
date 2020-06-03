## Annotation  与 Compiler 的 虐恋

  总所周知：ButterKnife 这个注解框架，实际上是通过拦截了Javac这个过程，那么Javac这个过程是将java文件或者其他文件，编译成
           class字节码文件，那么如果在这个时候进行拦截操作，进行Java文件的一些修改操作，然后将修改之后的Java文件与之前的Java源文件
           进行合并编译成class字节码文件，当然进行改造的部分代码一般都是比较重复的工作，即将这些重复的工作，在编译期间帮你做好，
           然后外界只需将这些重复的工作或者需要处理字段等信息进行标记，告知编译时期编译器需要对这些标记进行特殊的额外处理。
           那么Annotation是不是就可以编译Compile存在一些不可分割的联系呢
           
  关于使用：
    implementation 'com.google.auto.service:auto-service:1.0-rc6'
    annotationProcessor 'com.google.auto.service:auto-service:1.0-rc6'
    
    AutoServiceProcessor  extends  AbstractProcessor implements Processor 
    
    AutoServiceProcessor 实际 就是真实处理那些被标注的注解类，然后通过调用process方法将扫描这些注解java文件
    然后生成对应的修改文件，并且输出到特定的文件下，META-Service，生成的这些java文件会和java源文件进行合并编译
    成class字节码文件，那么重复工作的就在编译时期已经做好了，我们就可以将重复的代码工作用注解替代了。
    
    
    