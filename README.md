# 使用非常简单且功能强大的JAVA ORM框架


## POJO
```
public class AgentPo {

    private long aid;
    private long oid;
    private String csid;
    private String email;
    private String password;
    private String name;
    private String avatar;
    private int role;
    private int service_ceiling;
    private long group_id;
    private int forbidden;
}
```


## DAO
```

public class AgentDao extends MyBaseDAO<AgentPo>{
    private static Logger logger = LoggerFactory.getLogger(AgentDao.class);


    public AgentDao(){
        super(AgentPo.class,"cs_agent");
    }

    public AgentPo findById(long aid) throws Exception {
        return super.findOneByField("aid",aid);
    }


    public AgentPo getAgent(String email) throws Exception {
        //select * from cs_agent where email = ?
        return super.findOneByField("email", email);
    }

    public boolean isExistEmail(String email) throws Exception {
        //select count(0) from cs_agent where email = ?
        Long count = super.countByField("email", email);
        return count > 0;
    }


}

```



## MyBaseDAO

```

public class MyBaseDAO<T> extends DataAccessObject<T> {
    public MyBaseDAO(Class<T> clazz, String tableName) {
        super(clazz, tableName, MyConnectionFactory.getInstance());
    }
}

```
