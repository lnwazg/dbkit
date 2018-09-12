package com.lnwazg.dbkit.tools.graphtool.bean;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.swing.color.ColorUtils;

/**
 * 表结构
 * @author nan.li
 * @version 2017年4月19日
 */
public class GTable
{
    /**
     * 表名称
     */
    String tableName;
    
    /**
     * 表注释
     */
    String tableComment;
    
    /**
     * 该表的实际显示名称，格式为：表名称（注释）
     */
    String showName;
    
    /**
     * 表字段信息
     */
    List<GField> fields;
    
    int x;
    
    int y;
    
    int w;
    
    int h;
    
    /**
     * 背景色
     */
    Color bgColor = Color.WHITE;
    
    /**
     * 前景色
     */
    Color foreColor = Color.LIGHT_GRAY;
    
    /**
     * 矩形填充的背景色
     */
    Color fillRectBgColor = ColorUtils.str2Color("#FFFFE0");
    
    //    Color tableNameColor = Color.RED;
    
    /**
     * 表名称的颜色
     */
    //    Color tableNameColor = new Color(0xFF, 0x00, 0x84);//玫红
    Color tableNameColor = ColorUtils.str2Color("#FF3030");
    
    Font tableNameFont = new Font("微软雅黑", Font.ITALIC, 16);
    
    /**
     * 注释最大长度，超长的要被省略掉
     */
    int commentMaxLength = 25;
    
    public int getCommentMaxLength()
    {
        return commentMaxLength;
    }
    
    public GTable setCommentMaxLength(int commentMaxLength)
    {
        this.commentMaxLength = commentMaxLength;
        return this;
    }
    
    public String getTableName()
    {
        return tableName;
    }
    
    public GTable setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }
    
    public int getX()
    {
        return x;
    }
    
    public GTable setX(int x)
    {
        this.x = x;
        return this;
    }
    
    public int getY()
    {
        return y;
    }
    
    public GTable setY(int y)
    {
        this.y = y;
        return this;
    }
    
    public int getW()
    {
        return w;
    }
    
    public GTable setW(int w)
    {
        this.w = w;
        return this;
    }
    
    public int getH()
    {
        return h;
    }
    
    public GTable setH(int h)
    {
        this.h = h;
        return this;
    }
    
    @Override
    public String toString()
    {
        return GsonKit.prettyGson.toJson(this);
    }
    
    public Color getBgColor()
    {
        return bgColor;
    }
    
    public GTable setBgColor(Color bgColor)
    {
        this.bgColor = bgColor;
        return this;
    }
    
    public Color getForeColor()
    {
        return foreColor;
    }
    
    public GTable setForeColor(Color foreColor)
    {
        this.foreColor = foreColor;
        return this;
    }
    
    public Font getTableNameFont()
    {
        return tableNameFont;
    }
    
    public GTable setTableNameFont(Font tableNameFont)
    {
        this.tableNameFont = tableNameFont;
        return this;
    }
    
    public List<GField> getFields()
    {
        return fields;
    }
    
    public GTable setFields(List<GField> gFields)
    {
        this.fields = gFields;
        return this;
    }
    
    public Color getTableNameColor()
    {
        return tableNameColor;
    }
    
    public GTable setTableNameColor(Color tableNameColor)
    {
        this.tableNameColor = tableNameColor;
        return this;
    }
    
    public Color getFillRectBgColor()
    {
        return fillRectBgColor;
    }
    
    public GTable setFillRectBgColor(Color fillRectBgColor)
    {
        this.fillRectBgColor = fillRectBgColor;
        return this;
    }
    
    public String getTableComment()
    {
        return tableComment;
    }
    
    public GTable setTableComment(String tableComment)
    {
        this.tableComment = tableComment;
        return this;
    }
    
    public String getShowName()
    {
        return showName;
    }
    
    public GTable setShowName(String showName)
    {
        this.showName = showName;
        return this;
    }
}
