package com.kaway.db;

import com.kaway.beans.Dashboard;
import com.kaway.beans.DataPoint;
import com.kaway.beans.Security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BaseDAO {


    public void setDailySecData(String exchange, String secId, Map<String, List<DataPoint>> data) throws Exception;
    public Map<String, Object> getDailySecData(String exchange, String secId)throws Exception;
    public Map<String, Object> getSecList(String exchange)throws Exception;
    public void setSecList(String exchange, Map<String, List<Security>> data)throws Exception;
    public Map<String, Dashboard> getUserDashboards(String uid, String email)throws Exception;
    public void saveDashboard(String uid, String email, Map<String, Dashboard> data)throws Exception;
    public void deleteDashboard(String uid, String email, String dashboard)throws Exception;
}
