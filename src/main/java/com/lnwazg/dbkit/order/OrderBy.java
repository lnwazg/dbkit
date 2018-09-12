package com.lnwazg.dbkit.order;

/**
 * sql排序
 * @author nan.li
 * @version 2017年5月7日
 */
public class OrderBy
{
    public static final String ASC = "asc";
    
    public static final String DESC = "desc";
    
    private String col;
    
    private String direction;
    
    public OrderBy(String col, String direction)
    {
        this.col = col;
        setDirection(direction);
    }
    
    public OrderBy(String col)
    {
        this.col = col;
    }
    
    public String getCol()
    {
        return col;
    }
    
    public void setCol(String col)
    {
        this.col = col;
    }
    
    public String getDirection()
    {
        return direction;
    }
    
    public void setDirection(String direction)
    {
        if (direction != null && !ASC.equalsIgnoreCase(direction) && !DESC.equalsIgnoreCase(direction))
        {
            throw new IllegalArgumentException("Invalid Order Direction [" + direction + "]");
        }
        this.direction = direction;
    }
    
    @Override
    public String toString()
    {
        return direction != null ? col + " " + direction : col;
    }
}
