# 使用简单

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
