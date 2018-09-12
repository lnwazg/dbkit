package com.lnwazg.dbkit.tools.graphtool.bean;

import java.awt.Color;
import java.awt.Font;

import com.lnwazg.kit.swing.color.ColorUtils;

/**
 * 表字段的结构
 * @author nan.li
 * @version 2017年4月19日
 */
public class GField
{
    /**
     * 是否主键
     */
    boolean pk = false;
    
    /**
     * 字段名
     */
    String fieldName;
    
    /**
     * 字段类型
     */
    String fieldType;
    
    /**
     * 字段的评论信息
     */
    String comment;
    
    //整个行的位置
    int x;
    
    int y;
    
    int w;
    
    int h;
    
    //第1份的最低占比
    float p1 = 0.2F;
    
    //第2份的最低占比
    float p2 = 0.2F;
    
    /**
     * 字段间的间隔宽度
     */
    int fieldSeperateWidth = 5;
    
    //3等分
    /**
     * 每一行的第一个字段
     */
    int x1, y1, w1, h1;
    
    /**
     * 每一行的第二个字段
     */
    int x2, y2, w2, h2;
    
    /**
     * 每一行的第三个字段
     */
    int x3, y3, w3, h3;
    
    /**
     * 注释最大长度，超长的要被省略掉
     */
    int commentMaxLength = 22;
    
    /**
     * 背景色
     */
    Color bgColor = Color.WHITE;
    
    /**
     * 前景色
     */
    Color foreColor = Color.BLACK;
    
    Font fieldNameFont = new Font("monaco", Font.PLAIN, 14);
    
    Color fieldNameColor = Color.BLACK;
    
    Font fieldTypeFont = new Font("微软雅黑", Font.ITALIC, 13);
    
    Color fieldTypeColor = Color.DARK_GRAY;
    
    Font fieldCommentFont = new Font("楷体", Font.ITALIC, 16);
    
    //    Color fieldCommentColor = Color.dar;
    //亮绿色
    Color fieldCommentColor = ColorUtils.str2Color("#008B00");
    //    Color fieldCommentColor = Color.BLUE;
//        Color fieldCommentColor = Color.BLUE;
    //    Color fieldCommentColor = ColorUtils.str2Color("#00bfff");
    
    public boolean isPk()
    {
        return pk;
    }
    
    public GField setPk(boolean pk)
    {
        this.pk = pk;
        return this;
    }
    
    public Color getBgColor()
    {
        return bgColor;
    }
    
    public GField setBgColor(Color bgColor)
    {
        this.bgColor = bgColor;
        return this;
    }
    
    public Color getForeColor()
    {
        return foreColor;
    }
    
    public GField setForeColor(Color foreColor)
    {
        this.foreColor = foreColor;
        return this;
    }
    
    public int getX1()
    {
        return x1;
    }
    
    public GField setX1(int x1)
    {
        this.x1 = x1;
        return this;
    }
    
    public int getY1()
    {
        return y1;
    }
    
    public GField setY1(int y1)
    {
        this.y1 = y1;
        return this;
    }
    
    public int getW1()
    {
        return w1;
    }
    
    public GField setW1(int w1)
    {
        this.w1 = w1;
        return this;
    }
    
    public int getH1()
    {
        return h1;
    }
    
    public GField setH1(int h1)
    {
        this.h1 = h1;
        return this;
    }
    
    public int getX2()
    {
        return x2;
    }
    
    public GField setX2(int x2)
    {
        this.x2 = x2;
        return this;
    }
    
    public int getY2()
    {
        return y2;
    }
    
    public GField setY2(int y2)
    {
        this.y2 = y2;
        return this;
    }
    
    public int getW2()
    {
        return w2;
    }
    
    public GField setW2(int w2)
    {
        this.w2 = w2;
        return this;
    }
    
    public int getH2()
    {
        return h2;
    }
    
    public GField setH2(int h2)
    {
        this.h2 = h2;
        return this;
    }
    
    public int getX3()
    {
        return x3;
    }
    
    public GField setX3(int x3)
    {
        this.x3 = x3;
        return this;
    }
    
    public int getY3()
    {
        return y3;
    }
    
    public GField setY3(int y3)
    {
        this.y3 = y3;
        return this;
    }
    
    public int getW3()
    {
        return w3;
    }
    
    public GField setW3(int w3)
    {
        this.w3 = w3;
        return this;
    }
    
    public int getH3()
    {
        return h3;
    }
    
    public GField setH3(int h3)
    {
        this.h3 = h3;
        return this;
    }
    
    public int getX()
    {
        return x;
    }
    
    public GField setX(int x)
    {
        this.x = x;
        return this;
    }
    
    public int getY()
    {
        return y;
    }
    
    public GField setY(int y)
    {
        this.y = y;
        return this;
    }
    
    public int getW()
    {
        return w;
    }
    
    public GField setW(int w)
    {
        this.w = w;
        return this;
    }
    
    public int getH()
    {
        return h;
    }
    
    public GField setH(int h)
    {
        this.h = h;
        return this;
    }
    
    public int getCommentMaxLength()
    {
        return commentMaxLength;
    }
    
    public GField setCommentMaxLength(int commentMaxLength)
    {
        this.commentMaxLength = commentMaxLength;
        return this;
    }
    
    public String getFieldName()
    {
        return fieldName;
    }
    
    public GField setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
        return this;
    }
    
    public String getFieldType()
    {
        return fieldType;
    }
    
    public GField setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
        return this;
    }
    
    public String getComment()
    {
        return comment;
    }
    
    public GField setComment(String comment)
    {
        this.comment = comment;
        return this;
    }
    
    //    @Override
    //    public String toString()
    //    {
    //        return GsonHelper.prettyGson.toJson(this);
    //    }
    
    public Font getFieldNameFont()
    {
        return fieldNameFont;
    }
    
    public GField setFieldNameFont(Font fieldNameFont)
    {
        this.fieldNameFont = fieldNameFont;
        return this;
    }
    
    public Color getFieldNameColor()
    {
        return fieldNameColor;
    }
    
    public GField setFieldNameColor(Color fieldNameColor)
    {
        this.fieldNameColor = fieldNameColor;
        return this;
    }
    
    public Font getFieldTypeFont()
    {
        return fieldTypeFont;
    }
    
    public GField setFieldTypeFont(Font fieldTypeFont)
    {
        this.fieldTypeFont = fieldTypeFont;
        return this;
    }
    
    public Color getFieldTypeColor()
    {
        return fieldTypeColor;
    }
    
    public GField setFieldTypeColor(Color fieldTypeColor)
    {
        this.fieldTypeColor = fieldTypeColor;
        return this;
    }
    
    public Font getFieldCommentFont()
    {
        return fieldCommentFont;
    }
    
    public GField setFieldCommentFont(Font fieldCommentFont)
    {
        this.fieldCommentFont = fieldCommentFont;
        return this;
    }
    
    public Color getFieldCommentColor()
    {
        return fieldCommentColor;
    }
    
    public GField setFieldCommentColor(Color fieldCommentColor)
    {
        this.fieldCommentColor = fieldCommentColor;
        return this;
    }
    
    public float getP1()
    {
        return p1;
    }
    
    public GField setP1(float p1)
    {
        this.p1 = p1;
        return this;
    }
    
    public float getP2()
    {
        return p2;
    }
    
    public GField setP2(float p2)
    {
        this.p2 = p2;
        return this;
    }
    
    public int getFieldSeperateWidth()
    {
        return fieldSeperateWidth;
    }
    
    public GField setFieldSeperateWidth(int fieldSeperateWidth)
    {
        this.fieldSeperateWidth = fieldSeperateWidth;
        return this;
    }
}
