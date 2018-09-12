package com.lnwazg.dbkit.tools.sqlmonitor.entity;

public class LayerUITableDataObj
{
    int code;
    
    int count;
    
    Object data;
    
    String msg;
    
    public LayerUITableDataObj success()
    {
        code = 0;
        return this;
    }
    
    public LayerUITableDataObj fail(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
        return this;
    }
    
    public int getCode()
    {
        return code;
    }
    
    public LayerUITableDataObj setCode(int code)
    {
        this.code = code;
        return this;
    }
    
    public int getCount()
    {
        return count;
    }
    
    public LayerUITableDataObj setCount(int count)
    {
        this.count = count;
        return this;
    }
    
    public Object getData()
    {
        return data;
    }
    
    public LayerUITableDataObj setData(Object data)
    {
        this.data = data;
        return this;
    }
    
    public String getMsg()
    {
        return msg;
    }
    
    public LayerUITableDataObj setMsg(String msg)
    {
        this.msg = msg;
        return this;
    }
}
