# lms2000-sql-father
sql生成工具，轻松生成测试数据
# SQL-father项目模块大致介绍

![](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areasql-father.png)

## 用户管理

用户登录，注册，修改，删除等操作。



`密码加密`  使用的是固定的salt,md5加密

`登录的权限控制`  使用自定义的注解`@AuthCheck` +AOP  每次访问方法的时候判断注解中的anyRoles 是否为空，因为如果登录的话肯定会有role的，anyRoles为空就说明没有登录，拦截。 然后再判断mustRole 有没有，因为有些接口都是要特定权限role才可以访问的。



`登录状态` 用户登录后将用户信息存放在session中。







![img](https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/1/1666144811181-37d5bd7f-28fa-4b17-9147-ae7de8de1585-20221019132502647-20221019132511901.png)





## 表信息管理

表信息的增删改查

以及将schema转换为sql语句返回

其他管理模块都是类似的



## 解析SQL的思路



首先其将schema转换的sql以下(例子)：

```
//-- 组织管理表
//create table if not exists graduationproject.`t_organization`
//(
//`id` varchar(256) not null comment '主键' primary key,
//`o_name` varchar(256) null comment '党组织名称',
//`o_code` varchar(256) null comment '党组织代码',
//`o_category` varchar(256) null comment '党组织类别',
//`o_entered` varchar(256) null comment '已录入人数',
//`o_username` varchar(256) null comment '联系人姓名',
//`o_phone` varchar(256) not null comment '联系人联系方式',
//`is_deleted` varchar(256) not null comment '状态码'
//) comment '组织管理表';
```

项目将解析封装sql的表名，字段名的方法都放在core/sql下，有三个文件

![image-20230105194648081](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230105194648081.png)



其中SQLDialect接口类用来定义解析获取字段的方法， MySQLDialect来实现。

`SQLDialectFactory`   采用了单例+工厂设计模式来减少创建Dialect的 开销，  并且保证了线程安全。





![image-20230105212704460](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230105212704460.png)

`DataBiluder` 用来封装伪造数据的，并返回list.    其中每个元素就是数据库插入语句中的  `values()` 



`SqlBuilder` 用来获取建表sql字符串，获取插入语句，



`建表sql的思路`：  

首先我们需要一个sql模板如下

```java
String template = "%s\n" +
                "create table if not exists %s\n" +
                "(\n" +
                "%s\n" +
                ") %s; ";
```

其中收尾两个%s是用来填写表注释的，第二个%s是用来填充表名，第三个是用来填充字段信息的

然后我们通过schema获取这些信息然后填充返回。



## 伪造数据的策略

1. 不模拟 ： 如果字段是主键的话就按照模拟参数递增（模拟参数为空就用”1“代替），否则就用其默认值
2. 递增 ： 按照模拟参数（参数为空就）进行递增
3. 固定 ： 按照模拟参数创建，参数为空就用：”6“代替
4. 随机 ： 根据模拟参数（字符串,人名,城市,网址,邮箱,IP,整数,小数,大学,日期,时间戳,手机号)使用fakeUtils去随机生成数据
5. 规则 ：根据正则表达式去生成数据
6. 词库：根据词库内容列表随机生成数据

每一种策略都是一个单独的生成器类，最后通过生成器工厂来获取指定策略的生成器

![](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230105194648081.png)



## 生成insert语句思路



跟建表sql类似，首先创建insert语句的模板：

```java
insert into %s (%s) values (%s);
```

然后根据schema获取字段信息，接收生成数据列表，进行一系列的操作处理后填充到字符串中返回

项目中使用了 `SqlBuilder`类去处理构建语句，但是生成的字段数据是`DataBuilder`生成的



## 获取java实体类代码



![](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230110152913627.png)





使用模板，类似与上面的字符串模板，然后填充数据



## 解析sql语句

```
* 根据建表 SQL 构建
*使用druid提供的sql解析器MySqlCreateTableParser去解析sql，可以从SQLCreateTableStatement对象中获取
* 表名，数据库名，表注释
* SQLTableElement（SQLCreateTableStatement提供的getTableElementList）可以获取字段的列表信息
* 字段信息类要判断两种情况，一种是一般字段类SQLColumnDefinition，另一种是SQLPrimaryKey主键
* 最后封装结果集，返回schema
```





## 智能构表

```
智能构建
*首先根据  “,”或者“.”去分割content（content就是字段信息，如a,b,c,x等）
* 然后去数据库查找名称或者字段名字（名称是指字段的中文名称，字段名称是字母），然后去遍历拆分的content，如果说名称和字段名称都找不到就添加默认的字段信息
```







## 项目亮点

仅对于后端



1. 采用了单例+工厂还有门面模式，生成器模式，多种设计模式
2. 项目代码相对规范
3. 使用faker伪造数据





一些细节：

单例+工厂模式，并且用了String.intern来锁字符串,因为如果是锁的字符串的话，那么就不能保证锁的是同一个字符串，这样的话就不能保证单例了

```java
  if (null == sqlDialect) {
            ////双重检测加同步锁
            //public class LazyMan {
            //    private static LazyMan lazyMan;
            //    public LazyMan(){
            //        System.out.println(Thread.currentThread().getName()+"ok");
            //    }
            //    public static  LazyMan getInstance(){
            //        if(lazyMan==null){
            //            synchronized (LazyMan.class){
            //                if(lazyMan==null){
            //                    lazyMan=new LazyMan();
            //                }
            //            }
            //        }
            //        return lazyMan;
            //    }
            //}
            //使用同步块是因为在高并发的情况下保证单例
            synchronized (className.intern()) {
                //computeIfAbsent 方法 ：如果这个key存在就对这个value进行操作，如果不存在就创建，然后进行操作
                sqlDialect = DIALECT_POOL.computeIfAbsent(className,
                        key -> {
                            try {
                                return (SQLDialect) Class.forName(className).newInstance();
                            } catch (Exception ex) {
                                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                            }
                        });
            }
        }
```



`intern方法`  首先intern方法是一个native方法，底层是调用的C++的方法，当通过调用str.intern后，JVM会在当前类的常量池中查找是否存在与str等值的字符串，如果存在就返回常量池中的引用，若不存在就在常量池中创建一个等值的字符串然后返回引用。





intern 方法在JDK6与JDK7/8的区别



（1）在jdk6中，字符串常量池在永久代，调用itern()方法时，若常量池中不存在等值的字符串，JVM就会在字符串常量池中创建一个等值的字符串，然后返回该字符串的引用；

（2）在jdk7/8中，字符串常量池被移到了堆空间中，调用intern()方法时，如果常量池已经存在该字符串，则直接返回字符串引用，否则复制该堆空间中字符串对象到常量池中并返回。




注册时使用锁保证注册的账号不会冲突

```java
  //使用同步块来保证在高并发的环境下,同一时间有很多人用同一个账号注册冲突
       synchronized (userAccount.intern()){
           QueryWrapper<User> wrapper=new QueryWrapper<>();
           wrapper.eq("userAccount",userAccount);
           Long aLong = this.userMapper.selectCount(wrapper);
           if(aLong>0){
               throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在！");
           }

           // 3. 插入数据
           User user = new User();
           user.setUserName(userName);
           user.setUserAccount(userAccount);
           String encryptPassword= DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
           user.setUserPassword(encryptPassword);
           user.setUserRole(userRole);

           int insert = this.userMapper.insert(user);
           if(insert<0){
               throw new BusinessException(ErrorCode.OPERATION_ERROR,"注册失败");
           }
           return user.getId();
       }
```

























