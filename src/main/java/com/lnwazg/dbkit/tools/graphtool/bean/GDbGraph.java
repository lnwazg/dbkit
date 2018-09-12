package com.lnwazg.dbkit.tools.graphtool.bean;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.str.StringKit;
import com.lnwazg.kit.swing.image.GraphicsKit;

/**
 * 数据库表结构图
 * @author nan.li
 * @version 2017年4月19日
 */
public class GDbGraph
{
    /**
     * 图宽度多少像素<br>
     * 由最终计算得出
     */
    int graphWidth;
    
    /**
     * 图高多少像素<br>
     * 由最终计算得出
     */
    int graphHeight;
    
    /**
     * 每张表格的宽度
     */
    int tableWidth = 430;
    
    /**
     * 每行有多少列表格<br>
     * 默认值为5列
     */
    int columns = 5;
    
    /**
     * 一共有多少行表格图
     */
    int rows;
    
    /**
     * 表格之间的横向Margin
     */
    int tableMarginX = 10;
    
    /**
     * 表格之间的纵向Margin
     */
    int tableMarginY = 10;
    
    /**
     * 表格的行高
     */
    int tableFieldRowHeight = 25;
    
    /**
     * 数据库名称的标题的高度
     */
    int graphTitleHeight = 75;
    
    /**
     * 注释最大长度
     */
    int commentMaxLength = 10;
    
    /**
     * 数据库的名称
     */
    String dbName;
    
    /**
     * 导出的图片的最终显示标题
     */
    String title;
    
    Font titleFont = new Font("微软雅黑", Font.BOLD, 20);
    
    /**
     * 表数据列表
     */
    List<GTable> tables;
    
    /**
     * 背景色
     */
    Color bgColor = Color.WHITE;
    
    /**
     * 前景色
     */
    Color foreColor = Color.BLACK;
    
    /**
     * 该对象是否已经构建完毕
     */
    boolean isBuild;
    
    public Color getForeColor()
    {
        return foreColor;
    }
    
    public GDbGraph setForeColor(Color foreColor)
    {
        this.foreColor = foreColor;
        return this;
    }
    
    public Color getBgColor()
    {
        return bgColor;
    }
    
    public GDbGraph setBgColor(Color bgColor)
    {
        this.bgColor = bgColor;
        return this;
    }
    
    public int getGraphWidth()
    {
        return graphWidth;
    }
    
    public GDbGraph setGraphWidth(int graphWidth)
    {
        this.graphWidth = graphWidth;
        return this;
    }
    
    public int getGraphHeight()
    {
        return graphHeight;
    }
    
    public GDbGraph setGraphHeight(int graphHeight)
    {
        this.graphHeight = graphHeight;
        return this;
    }
    
    public int getColumns()
    {
        return columns;
    }
    
    public GDbGraph setColumns(int columns)
    {
        this.columns = columns;
        return this;
    }
    
    public int getRows()
    {
        return rows;
    }
    
    public void setRows(int rows)
    {
        this.rows = rows;
    }
    
    public int getTableWidth()
    {
        return tableWidth;
    }
    
    public void setTableWidth(int tableWidth)
    {
        this.tableWidth = tableWidth;
    }
    
    public int getTableFieldRowHeight()
    {
        return tableFieldRowHeight;
    }
    
    public void setTableFieldRowHeight(int tableFieldRowHeight)
    {
        this.tableFieldRowHeight = tableFieldRowHeight;
    }
    
    public int getGraphTitleHeight()
    {
        return graphTitleHeight;
    }
    
    public void setGraphTitleHeight(int graphTitleHeight)
    {
        this.graphTitleHeight = graphTitleHeight;
    }
    
    public int getTableMarginX()
    {
        return tableMarginX;
    }
    
    public GDbGraph setTableMarginX(int tableMarginX)
    {
        this.tableMarginX = tableMarginX;
        return this;
    }
    
    public int getTableMarginY()
    {
        return tableMarginY;
    }
    
    public GDbGraph setTableMarginY(int tableMarginY)
    {
        this.tableMarginY = tableMarginY;
        return this;
    }
    
    public String getDbName()
    {
        return dbName;
    }
    
    public GDbGraph setDbName(String dbName)
    {
        this.dbName = dbName;
        return this;
    }
    
    public List<GTable> getTables()
    {
        return tables;
    }
    
    public GDbGraph setTables(List<GTable> tables)
    {
        this.tables = tables;
        return this;
    }
    
    @Override
    public String toString()
    {
        return GsonKit.prettyGson.toJson(this);
    }
    
    /**
     * 构建各种尺寸
     * 对表进行排序，并重新计算各种尺寸
     * @author nan.li
     * @return
     */
    public GDbGraph build()
    {
        //对列表进行排序
        Collections.sort(tables, (t1, t2) -> {
            //按表名称字符顺序从小到大排序（字典顺序排序）
            return t1.getTableName().compareTo(t2.getTableName());
        });
        
        //根据数据库名称重设标题栏
        title = String.format("数据库%s的表结构图", StringUtils.isEmpty(dbName) ? "" : dbName);
        
        //表的数量
        int tableSize = tables.size();
        
        //列数为columns字段
        //计算一共要绘制多少行数据
        //行数
        rows = tableSize % columns == 0 ? tableSize / columns : (tableSize / columns + 1);
        
        //图的宽度计算（列宽*列数 + 间隙总和）
        graphWidth = columns * tableWidth + (columns + 1) * tableMarginX;
        
        //图的高度(初始化为标题的高度，后面逐步根据需要增加高度)
        graphHeight = graphTitleHeight;
        
        //表数据深度计算
        int thisMaxH = 0;//本行最高值
        int lastMaxH = 0;//上行最高值
        int lastMaxHSum = 0;//上面最高的汇总值 
        
        //遍历表数据
        for (int i = 0; i < tableSize; i++)
        {
            //表格对象
            GTable table = tables.get(i);
            
            //是否刚开启了新的一行
            boolean newLine = (i % columns == 0);
            if (newLine)
            {
                //记录上一行最高值
                lastMaxH = thisMaxH;//这个是为了辅助定位lastMaxHSum，也就是说，变相地辅助定位了Table的y坐标
                lastMaxHSum += lastMaxH;//这个是为了辅助定位Table的y坐标
                //reset本行最高值
                thisMaxH = 0;
                
                //每开启新的一行，就要加上总高度
                graphHeight += (lastMaxH + tableMarginY);//追加上上一行的最大高度+间隙
            }
            
            //行号，从0开始计数
            int rowNum = i / columns;               //0  0  0   1   1   1   2   2   2
            
            //列号，从0开始计数
            int columnNum = i - columns * rowNum;   //0  1  2   0   1   2   0   1   2
            
            //宽高
            int w = tableWidth;
            int h = (table.getFields().size() + 1) * tableFieldRowHeight;//标题也算一行，因此总行数是表格的行数+1
            
            //记录本行最高值
            if (h > thisMaxH)
            {
                thisMaxH = h;//每次更新完最高值
            }
            
            //x,y坐标
            int x = tableMarginX * (columnNum + 1) + tableWidth * columnNum;
            int y = graphTitleHeight + tableMarginY * rowNum + lastMaxHSum;
            
            //字段列表
            List<GField> fields = table.getFields();
            int fieldLength = fields.size();
            
            //动态宽度的实现
            int _w1 = 0;
            int _w2 = 0;
            
            for (int j = 0; j < fieldLength; j++)
            {
                GField field = fields.get(j);
                int w0 = w;
                int w1 = (int)(w0 * (field.getP1()));
                int wordWidth1 = GraphicsKit.getStrWidthByFontAndStr(field.getFieldNameFont(), field.getFieldName());
                if (w1 > _w1)
                {
                    _w1 = w1;
                }
                if (wordWidth1 > _w1)
                {
                    _w1 = wordWidth1;
                }
                
                int w2 = (int)(w0 * (field.getP2()));
                int wordWidth2 = GraphicsKit.getStrWidthByFontAndStr(field.getFieldTypeFont(), field.getFieldType());
                if (w2 > _w2)
                {
                    _w2 = w2;
                }
                if (wordWidth2 > _w2)
                {
                    _w2 = wordWidth2;
                }
            }
            
            for (int j = 0; j < fieldLength; j++)
            {
                //获取那个字段的对象
                GField field = fields.get(j);
                int x0 = x;
                int y0 = y + (j + 1) * tableFieldRowHeight;
                int w0 = w;
                int h0 = tableFieldRowHeight;
                
                int x1 = x0;
                int y1 = y0;
                //                int w1 = (int)(w0 * (field.getP1()));
                int w1 = _w1;
                int h1 = h0;
                
                int x2 = x1 + w1 + field.getFieldSeperateWidth();
                
                int y2 = y0;
                //                int w2 = (int)(w0 * (field.getP2()));
                int w2 = _w2;
                int h2 = h0;
                
                int x3 = x2 + w2 + field.getFieldSeperateWidth();
                int y3 = y0;
                //                int w3 = (int)(w0 * (1 - field.getP1() - field.getP2()));
                int w3 = w - w1 - w2;
                int h3 = h0;
                
                field.setX(x0)
                    .setY(y0)
                    .setW(w0)
                    .setH(h0)
                    .setX1(x1)
                    .setY1(y1)
                    .setW1(w1)
                    .setH1(h1)
                    .setX2(x2)
                    .setY2(y2)
                    .setW2(w2)
                    .setH2(h2)
                    .setX3(x3)
                    .setY3(y3)
                    .setW3(w3)
                    .setH3(h3);
                    
                //裁剪注释长度
                field.setComment(StringKit.abbreviate(field.getComment(), field.getCommentMaxLength()));
                
                //字段类型 int() 精简为int
                String fieldType = field.getFieldType();
                if (StringUtils.isNotEmpty(fieldType))
                {
                    if (fieldType.indexOf("()") != -1)
                    {
                        fieldType = fieldType.substring(0, fieldType.indexOf("()"));
                        field.setFieldType(fieldType);
                    }
                }
            }
            
            //设置当前表的坐标属性
            table.setX(x).setY(y).setW(w).setH(h);
            
            String tableComment = table.getTableComment();
            //设置显示名称
            table.setShowName(String.format("%s %s",
                table.getTableName(),
                StringUtils.isNotEmpty(tableComment) ? String.format("(%s)", StringKit.abbreviate(tableComment, table.getCommentMaxLength())) : ""));
                
        }
        
        //最后还要追加上上一行的最大高度+间隙，才能完美结束
        graphHeight += (thisMaxH + tableMarginY);
        
        //对象终于已经被完整地构建完毕
        isBuild = true;
        
        return this;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public GDbGraph setTitle(String title)
    {
        this.title = title;
        return this;
    }
    
    public Font getTitleFont()
    {
        return titleFont;
    }
    
    public GDbGraph setTitleFont(Font titleFont)
    {
        this.titleFont = titleFont;
        return this;
    }
    
    public int getCommentMaxLength()
    {
        return commentMaxLength;
    }
    
    public GDbGraph setCommentMaxLength(int commentMaxLength)
    {
        this.commentMaxLength = commentMaxLength;
        return this;
    }
    
    public boolean isBuild()
    {
        return isBuild;
    }
}
