package com.lnwazg.dbkit.tools.graphtool;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.ImageIO;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.db2javabean.DbTableStructureKit;
import com.lnwazg.dbkit.tools.graphtool.bean.GDbGraph;
import com.lnwazg.dbkit.tools.graphtool.bean.GField;
import com.lnwazg.dbkit.tools.graphtool.bean.GTable;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.io.StreamUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.swing.image.Align;
import com.lnwazg.kit.swing.image.ImageKit;

/**
 * 输出类似PowerDesigner功能的数据库表结构图<br>
 * 1. 做出数据库表结构元数据
 * 2. 计算出每个元数据的宽高细节占比
 * 3. 绘图
 * @author nan.li
 * @version 2017年4月19日
 */
public class DbGraphTool
{
    private static byte[] drawGraphToBytes(GDbGraph dbTableGraph)
    {
        if (!dbTableGraph.isBuild())
        {
            Logs.e("GDbGraph对象内部细节尚未构建完毕，无法使用！");
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try
        {
            BufferedImage image = buildImage(dbTableGraph);
            ImageIO.write(image, "png", byteArrayOutputStream);
            Logs.i("Draw OK!");
            return byteArrayOutputStream.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(byteArrayOutputStream);
        }
        return null;
    }
    
    /**
    * 根据数据进行画图即可
    * @author nan.li
    * @param imgPath
    * @param dbTableGraph
    */
    public static void drawGraph(String imgPath, GDbGraph dbTableGraph)
    {
        if (!dbTableGraph.isBuild())
        {
            Logs.e("GDbGraph对象内部细节尚未构建完毕，无法使用！");
            return;
        }
        File imgFile = new File(imgPath);
        FileOutputStream fileOutputStream = null;
        try
        {
            BufferedImage image = buildImage(dbTableGraph);
            fileOutputStream = new FileOutputStream(imgFile);
            ImageIO.write(image, "png", fileOutputStream);
            Logs.i("Draw OK!");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(fileOutputStream);
        }
    }
    
    /**
     * 构建图
     * @author nan.li
     * @param dbTableGraph
     * @return
     */
    private static BufferedImage buildImage(GDbGraph dbTableGraph)
    {
        // 在内存中创建图象
        BufferedImage image = new BufferedImage(dbTableGraph.getGraphWidth(), dbTableGraph.getGraphHeight(), BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        Graphics2D g = image.createGraphics();
        // 设定背景色
        g.setColor(dbTableGraph.getBgColor());
        //填充设定的背景色到指定区域
        g.fillRect(0, 0, dbTableGraph.getGraphWidth(), dbTableGraph.getGraphHeight());
        
        //绘制标题
        //在指定区域居中绘制字符串
        ImageKit.drawString(g,
            0,
            0,
            dbTableGraph.getGraphWidth(),
            dbTableGraph.getGraphTitleHeight(),
            dbTableGraph.getTitle(),
            dbTableGraph.getTitleFont(),
            dbTableGraph.getForeColor());
            
        List<GTable> tables = dbTableGraph.getTables();
        for (GTable gTable : tables)
        {
            drawTable(g, gTable, dbTableGraph);
        }
        g.dispose();
        return image;
    }
    
    /**
     * 依次绘制表格
     * @author nan.li
     * @param g
     * @param gTable
     */
    private static void drawTable(Graphics2D g, GTable gTable, GDbGraph dbTableGraph)
    {
        //先绘制矩形表格框
        ImageKit.drawRoundRect(g, gTable.getX(), gTable.getY(), gTable.getW(), gTable.getH(), 0, gTable.getForeColor());
        
        //矩形填充
        ImageKit.fillRoundRect(g, gTable.getX(), gTable.getY(), gTable.getW(), gTable.getH(), 0, gTable.getFillRectBgColor());
        
        //绘制表名称
        ImageKit.drawString(g,
            gTable.getX(),
            gTable.getY(),
            gTable.getW(),
            dbTableGraph.getTableFieldRowHeight(),
            gTable.getShowName(),
            gTable.getTableNameFont(),
            gTable.getTableNameColor());
            
        //依次绘制表字段内容
        
        for (GField gField : gTable.getFields())
        {
            ImageKit.drawString(g,
                gField.getX1(),
                gField.getY1(),
                gField.getW1(),
                gField.getH1(),
                gField.getFieldName(),
                gField.getFieldNameFont(),
                gField.getFieldNameColor(),
                Align.LEFT);
                
            ImageKit.drawString(g,
                gField.getX2(),
                gField.getY2(),
                gField.getW2(),
                gField.getH2(),
                gField.getFieldType(),
                gField.getFieldTypeFont(),
                gField.getFieldTypeColor(),
                Align.LEFT);
                
            ImageKit.drawString(g,
                gField.getX3(),
                gField.getY3(),
                gField.getW3(),
                gField.getH3(),
                gField.getComment(),
                gField.getFieldCommentFont(),
                gField.getFieldCommentColor(),
                Align.LEFT);
        }
    }
    
    /**
     * 从数据库中加载表结构数据
     * @author nan.li
     * @param jdbc
     * @return
     */
    public static GDbGraph loadGdbFromDb(MyJdbc jdbc)
    {
        GDbGraph dbTableGraph = new GDbGraph().setColumns(3).setDbName(DbKit.SCHEMA_NAME);
        try
        {
            List<GTable> tableList = DbTableStructureKit.getTableList(jdbc);
            dbTableGraph.setTables(tableList).build();
            return dbTableGraph;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 一键绘图
     * @author nan.li
     * @param imgPath
     * @param configPath
     */
    public static void drawGraph(String imgPath, String configPath)
    {
        MyJdbc jdbc = DbKit.getJdbc(configPath);
        GDbGraph dbTableGraph = DbGraphTool.loadGdbFromDb(jdbc);
        DbGraphTool.drawGraph(imgPath, dbTableGraph);
    }
    
    /**
     * 将数据库配置文件输出为图的字节码数组
     * @author nan.li
     * @param configPath
     * @return
     */
    public static byte[] drawGraphToBytes(String configPath)
    {
        MyJdbc jdbc = DbKit.getJdbc(configPath);
        GDbGraph dbTableGraph = DbGraphTool.loadGdbFromDb(jdbc);
        return DbGraphTool.drawGraphToBytes(dbTableGraph);
    }
    
    /**
     * 将MyJdbc输出为图的字节码数组
     * @author nan.li
     * @param jdbc
     * @return
     */
    public static byte[] drawGraphToBytes(MyJdbc jdbc)
    {
        GDbGraph dbTableGraph = DbGraphTool.loadGdbFromDb(jdbc);
        return DbGraphTool.drawGraphToBytes(dbTableGraph);
    }
}
