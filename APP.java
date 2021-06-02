import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.*;
import java.util.List;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class APP {
    static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    public static void main(String[] args) {
        //获取显示器分辨率

        //不包括任务栏的屏幕大小
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //得到屏幕的尺寸
       // System.out.println(screenSize.width+" "+screenSize.height);

        //包括任务栏的屏幕大小
        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle rect=ge.getMaximumWindowBounds();

        int w=rect.width;
        int h=rect.height;
        //System.out.println(w+" "+h);

        //创建 界面
        Mat dst = new Mat();
        JFrame jf = new JFrame("投影变换 v2.0");
        jf.setSize(w, h);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setLayout(new BorderLayout());
        // 创建 内容面板
        JPanel panel = new JPanel(new BorderLayout());
        //创建 标签
        JLabel label = new JLabel();
        label.setFont(new Font("宋体",Font.BOLD,20));
        label.setBounds(0, 0, w, h);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label,BorderLayout.CENTER);
        // 创建 工具栏
        JToolBar toolBar = new JToolBar("功能菜单栏");
        //读取图片路径与对应Mat
        final String[] filename = {new String()};
        final Mat[] source = {Imgcodecs.imread(filename[0])};
        //图片长宽
        final int[] ImgHeight = new int[1];
        final int[] ImgWidth = new int[1];
        //图像填充黑色背景的上下左右距离
        final int[] top = new int[1];
        final int[] bottom = new int[1];
        final int[] left = new int[1];
        final int[] right = new int[1];
        final int[] enableLine = {0};
        // 创建 按钮
        // 读取图片 按钮
        JButton ReadImage = new JButton("读取图片");
        ReadImage.setFont(new Font("宋体",Font.BOLD,20));
        // 四点标定 按钮
        JButton FourPoints = new JButton("四点标定");
        FourPoints.setFont(new Font("宋体",Font.BOLD,20));
        FourPoints.setEnabled(false);
        // 撤销标定 按钮
        JButton CancelPoints = new JButton("撤销标定");
        CancelPoints.setFont(new Font("宋体",Font.BOLD,20));
        CancelPoints.setEnabled(false);
        // 缺陷标注 按钮
        JButton DefectLable = new JButton("缺陷标注");
        DefectLable.setFont(new Font("宋体",Font.BOLD,20));
        DefectLable.setEnabled(false);
        // 取消标注 按钮
        JButton CancelLable = new JButton("取消标注");
        CancelLable.setFont(new Font("宋体",Font.BOLD,20));
        CancelLable.setEnabled(false);
        // 投影变换 按钮
        JButton Transform = new JButton("投影变换");
        Transform.setFont(new Font("宋体",Font.BOLD,20));
        Transform.setEnabled(false);
        // 原始变换 按钮
        JRadioButton rb1=new JRadioButton("原始变换");
        rb1.setFont(new Font("宋体",Font.BOLD,20));//创建JRadioButton对象
        rb1.setEnabled(false);
        // 图形变换 按钮
        JRadioButton rb2=new JRadioButton("图形变换");    //创建JRadioButton对象
        rb2.setFont(new Font("宋体",Font.BOLD,20));//创建JRadioButton对象
        rb2.setEnabled(false);
        // 标注形状 按钮
        JRadioButton circle_shape=new JRadioButton("圆形");    //创建JRadioButton对象
        circle_shape.setFont(new Font("宋体",Font.BOLD,20));//创建JRadioButton对象
        circle_shape.setEnabled(false);
        JRadioButton poly_shape=new JRadioButton("多边形");    //创建JRadioButton对象
        poly_shape.setFont(new Font("宋体",Font.BOLD,20));//创建JRadioButton对象
        poly_shape.setEnabled(false);
        // 将 原始变换 与 图形变换 汇总为单选按钮组
        ButtonGroup group=new ButtonGroup();
        group.add(rb1);
        group.add(rb2);
        ButtonGroup group2=new ButtonGroup();
        group2.add(circle_shape);
        group2.add(poly_shape);

        // 添加 按钮 到 工具栏
        toolBar.add(ReadImage);
        toolBar.add(FourPoints);
        toolBar.add(CancelPoints);
        toolBar.add(DefectLable);
        toolBar.add(CancelLable);
        toolBar.add(Transform);
        toolBar.add(circle_shape);
        toolBar.add(poly_shape);
        toolBar.add(rb1);
        toolBar.add(rb2);
        toolBar.setOrientation(SwingConstants.HORIZONTAL);

        // 启动提示（务必保证界面全屏，使界面坐标与屏幕坐标一致）
        JOptionPane jopt = new JOptionPane();
        String result;
        result = "使用本程序时，请先点击右上角窗口最大化！";
        JLabel resLabel = new JLabel(result);
        resLabel.setFont(new Font("宋体", Font.BOLD, 20));
        jopt.showMessageDialog( null, resLabel);

        // 使能是否缺陷绘制标志位，若不绘制，则只能进行原始投影
        final Boolean[] EnableTrans = new Boolean[1];
        // 完成四点标定的 Mat图像 source1
        final Mat[] source1 = {Imgcodecs.imread(filename[0])};
        // 完成缺陷标注的 Mat图像 source3
        final Mat[] source3 ={Imgcodecs.imread(filename[0])};
        // 用于图形变换的 Mat图像 black
        final Mat[] black = {new Mat()};
        // 储存原始标定四点的向量
        Vector<Point> fourPoints=new Vector<Point>(4,1);
        // 缺陷存储向量
        Vector<defect> defects=new Vector<defect>(10,1);
        final Vector<Point>[] poly_defects = new Vector[]{new Vector<Point>(20, 1)};
        Vector<Point> test=new Vector<Point>(20,1);
        Vector<Vector<Point>> poly_defects_list=new Vector<Vector<Point>>(20,1);
        Point org=new Point();
        final defect[] d = {new defect()};
        //新位置的四点坐标
        final Point[] cursorA = {new Point()};
        final Point[] cursorB = {new Point()};
        final Point[] cursorC = {new Point()};
        final Point[] cursorD = {new Point()};

        // 四点标定的鼠标监听 M1
        MouseListener M1 = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                source[0].copyTo(source1[0]);
                //Core.copyMakeBorder(source1[0],source1[0],top,bottom,left,right,0);
               // Imgproc.resize(source1[0],source1[0],new Size(1920,1010));
                Point cursor0 = new Point(e.getX(), e.getY());
                if(fourPoints.size()<4) {
                    fourPoints.add(cursor0);
                    for (int i = 0; i < fourPoints.size(); i++) {
                        System.out.println(fourPoints.elementAt(i));
                        Imgproc.circle(source1[0], fourPoints.elementAt(i), 5, new Scalar(0, 0, 255), 2);
                        if(i>0) {
                            Imgproc.line(source1[0], fourPoints.elementAt(i-1), fourPoints.elementAt(i), new Scalar(0, 255, 0),1,Imgproc.LINE_AA);
                        }
                        if(i==3){
                            Imgproc.line(source1[0], fourPoints.elementAt(i), fourPoints.elementAt(i%3), new Scalar(0, 255, 0),1,Imgproc.LINE_AA);
                            DefectLable.setEnabled(true);
                            Transform.setEnabled(true);
                        }
                    }
                    BufferedImage image1 = mat2BufferedImage.matToBufferedImage(source1[0]);
                    label.setIcon(new ImageIcon(image1));
                    System.out.println(fourPoints.size());
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
        // 缺陷标注的鼠标监听 M2
        MouseListener M2 = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Press");
                org.x=e.getX();
                org.y=e.getY();
                //Imgproc.line(source1[0], new Point(e.getX(),e.getY()), fourPoints.elementAt(i), new Scalar(0, 255, 0));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Release");
                System.out.println("d.x:"+ d[0].x+" d.y:"+ d[0].y);
                defects.addElement(d[0]);
                for (int i = 0; i < defects.size(); i++) {
                    System.out.println("x:"+defects.elementAt(i).x+" y:"+defects.elementAt(i).y+";radius:"+defects.elementAt(i).radius);
                    //System.out.println(test.elementAt(i));
                }
                d[0] =new defect();
               // System.out.println("x:"+d.x+" y:"+d.y+";radius:"+d.radius);

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
        // 缺陷标注的鼠标监听 M3
        MouseListener M3 = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point label=new Point();
                label.x=e.getX();
                label.y=e.getY();
//                Imgproc.circle(source3[0], label, 5, new Scalar(0, 0, 255), 2);
//                Imgproc.circle(black[0], label, 5, new Scalar(0, 0, 255), 2);
                poly_defects[0].addElement(label);
                System.out.println("Click");
//                for (int q = 0; q < poly_defects.size(); q++) {
//                    System.out.println(poly_defects.elementAt(q));
//                }
                enableLine[0] =1;
                if(poly_defects[0].size()>2) {
                    boolean in_start_point = (e.getX()) > (poly_defects[0].elementAt(0).x - 10) &&
                            (e.getX()) < (poly_defects[0].elementAt(0).x + 10) &&
                            (e.getY()) > (poly_defects[0].elementAt(0).y - 10) &&
                            (e.getY()) < (poly_defects[0].elementAt(0).y + 10);
                    if (in_start_point) {
                        enableLine[0] = 2;
                        poly_defects[0].removeElementAt(poly_defects[0].size()-1);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };

        // 圆形缺陷标注的鼠标移动监听 M2_2
        MouseMotionListener M2_2 = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                source[0].copyTo(source3[0]);
                black[0] =Mat.zeros(h-70,w,CvType.CV_8UC3);
               // Imgproc.resize(source3[0],source3[0],new Size(1920,1010));
                for (int i = 0; i < fourPoints.size(); i++) {
                    Imgproc.circle(source3[0], fourPoints.elementAt(i), 5, new Scalar(0, 0, 255), 2);
                    Imgproc.circle(black[0], fourPoints.elementAt(i), 5, new Scalar(0, 0, 255), 2);
                    if(i>0) {
                        Imgproc.line(source3[0], fourPoints.elementAt(i-1), fourPoints.elementAt(i), new Scalar(0, 255, 0),1,Imgproc.LINE_AA);
                        Imgproc.line(black[0], fourPoints.elementAt(i-1), fourPoints.elementAt(i), new Scalar(0, 255, 0),1,Imgproc.LINE_AA);
                    }
                    if(i==3){
                        Imgproc.line(source3[0], fourPoints.elementAt(i), fourPoints.elementAt(i%3), new Scalar(0, 255, 0),1,Imgproc.LINE_AA);
                        Imgproc.line(black[0], fourPoints.elementAt(i), fourPoints.elementAt(i%3), new Scalar(0, 255, 0),1,Imgproc.LINE_AA);
                    }
                } //画线
                for (int i = 0; i < defects.size(); i++) {
                    Imgproc.circle(source3[0], new Point(defects.elementAt(i).x,defects.elementAt(i).y),defects.elementAt(i).radius, new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
                    Imgproc.circle(black[0], new Point(defects.elementAt(i).x,defects.elementAt(i).y),defects.elementAt(i).radius, new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
                }
                for (int q = 0; q < poly_defects[0].size(); q++) {
                    Imgproc.line(source3[0], poly_defects[0].elementAt(q-1), poly_defects[0].elementAt(q), new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
                    Imgproc.line(black[0], poly_defects[0].elementAt(q-1), poly_defects[0].elementAt(q), new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
                }
                if(poly_defects_list.size()>0) {
                    for (int k = 0; k < poly_defects_list.size(); k++) {
                        for(int p=0;p<poly_defects_list.elementAt(k).size();p++) {
                            if(p>0 && p<poly_defects_list.elementAt(k).size()-1) {
                                Imgproc.line(source3[0], poly_defects_list.elementAt(k).elementAt(p-1), poly_defects_list.elementAt(k).elementAt(p), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                                Imgproc.line(black[0], poly_defects_list.elementAt(k).elementAt(p-1), poly_defects_list.elementAt(k).elementAt(p), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                            }
                        }
                        Imgproc.line(source3[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 2), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                        Imgproc.line(black[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 2), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                        Imgproc.line(source3[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(0), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                        Imgproc.line(black[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(0), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                    }
                }
                Point current=new Point(e.getX(),e.getY());
                d[0].x=0.5*(org.x+current.x);
                d[0].y=0.5*(org.y+current.y);
                d[0].radius= (int) ((int) Math.sqrt((current.x-org.x)*(current.x-org.x)+(current.y-org.y)*(current.y-org.y))*0.5);
                Imgproc.circle(source3[0], new Point(d[0].x, d[0].y), d[0].radius, new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
                Imgproc.circle(black[0], new Point(d[0].x, d[0].y), d[0].radius, new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
                BufferedImage image3 = mat2BufferedImage.matToBufferedImage(source3[0]);
                label.setIcon(new ImageIcon(image3));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                System.out.println("org:  "+e.getX()+" "+e.getY());
                System.out.println("trans:"+(e.getX())+" "+(e.getY()));
            }
        };
        // 多边形缺陷标注的鼠标移动监听 M2_3
        MouseMotionListener M2_3 = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (enableLine[0]==1) {
                    source[0].copyTo(source3[0]);
                    black[0] = Mat.zeros(h - 70, w, CvType.CV_8UC3);
                    // Imgproc.resize(source3[0],source3[0],new Size(1920,1010));
                    for (int i = 0; i < fourPoints.size(); i++) {
                        Imgproc.circle(source3[0], fourPoints.elementAt(i), 5, new Scalar(0, 0, 255), 2);
                        Imgproc.circle(black[0], fourPoints.elementAt(i), 5, new Scalar(0, 0, 255), 2);
                        if (i > 0) {
                            Imgproc.line(source3[0], fourPoints.elementAt(i - 1), fourPoints.elementAt(i), new Scalar(0, 255, 0), 1, Imgproc.LINE_AA);
                            Imgproc.line(black[0], fourPoints.elementAt(i - 1), fourPoints.elementAt(i), new Scalar(0, 255, 0), 1, Imgproc.LINE_AA);
                        }
                        if (i == 3) {
                            Imgproc.line(source3[0], fourPoints.elementAt(i), fourPoints.elementAt(i % 3), new Scalar(0, 255, 0), 1, Imgproc.LINE_AA);
                            Imgproc.line(black[0], fourPoints.elementAt(i), fourPoints.elementAt(i % 3), new Scalar(0, 255, 0), 1, Imgproc.LINE_AA);
                        }
                    } //画线
                    for (int i = 0; i < defects.size(); i++) {
                        Imgproc.circle(source3[0], new Point(defects.elementAt(i).x, defects.elementAt(i).y), defects.elementAt(i).radius, new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                        Imgproc.circle(black[0], new Point(defects.elementAt(i).x, defects.elementAt(i).y), defects.elementAt(i).radius, new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                    }
                    Point cursor = new Point();
                    cursor.x = e.getX();
                    cursor.y = e.getY();
                    for (int q = 0; q < poly_defects[0].size(); q++) {
                        if (q > 0) {
                            Imgproc.line(source3[0], poly_defects[0].elementAt(q - 1), poly_defects[0].elementAt(q), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                            Imgproc.line(black[0], poly_defects[0].elementAt(q - 1), poly_defects[0].elementAt(q), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                        }
                    }
                    if(poly_defects_list.size()>0) {
                        for (int k = 0; k < poly_defects_list.size(); k++) {
                            for(int p=0;p<poly_defects_list.elementAt(k).size();p++) {
                                if(p>0){
                                    Imgproc.line(source3[0], poly_defects_list.elementAt(k).elementAt(p-1), poly_defects_list.elementAt(k).elementAt(p), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                                    Imgproc.line(black[0], poly_defects_list.elementAt(k).elementAt(p-1), poly_defects_list.elementAt(k).elementAt(p), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                                }
                            }
                            Imgproc.line(source3[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 2), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                            Imgproc.line(black[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 2), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                            Imgproc.line(source3[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(0), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                            Imgproc.line(black[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(0), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                        }
                    }
                    Imgproc.line(source3[0], poly_defects[0].elementAt(poly_defects[0].size() - 1), cursor, new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                    Imgproc.line(black[0], poly_defects[0].elementAt(poly_defects[0].size() - 1), cursor, new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
                    BufferedImage image3 = mat2BufferedImage.matToBufferedImage(source3[0]);
                    label.setIcon(new ImageIcon(image3));
                }
                else if(enableLine[0]==2){
                    poly_defects_list.add(poly_defects[0]);
                    System.out.println(poly_defects_list);
//                    for(int k=0;k<poly_defects_list.size();k++){
//                        Imgproc.line(source3[0], poly_defects_list.elementAt(k).elementAt(poly_defects_list.elementAt(k).size() - 1), poly_defects_list.elementAt(k).elementAt(0), new Scalar(0, 255, 255), 1, Imgproc.LINE_AA);
//                    }
                    BufferedImage image3 = mat2BufferedImage.matToBufferedImage(source3[0]);
                    label.setIcon(new ImageIcon(image3));
                    poly_defects[0] =new Vector<Point>();
                    enableLine[0]=0;
                }
            }
        };
        // 原图变换的鼠标移动监听 M3_1
        MouseMotionListener M3_1 = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                boolean in_Point_A=(e.getX())>(cursorA[0].x-40)&&
                        (e.getX())<(cursorA[0].x+40)&&
                        (e.getY())>(cursorA[0].y-40)&&
                        (e.getY())<(cursorA[0].y+40);
                boolean in_Point_B=(e.getX())>(cursorB[0].x-40)&&
                        (e.getX())<(cursorB[0].x+40)&&
                        (e.getY())>(cursorB[0].y-40)&&
                        (e.getY())<(cursorB[0].y+40);
                boolean in_Point_C=(e.getX())>(cursorC[0].x-40)&&
                        (e.getX())<(cursorC[0].x+40)&&
                        (e.getY())>(cursorC[0].y-40)&&
                        (e.getY())<(cursorC[0].y+40);
                boolean in_Point_D=(e.getX())>(cursorD[0].x-40)&&
                        (e.getX())<(cursorD[0].x+40)&&
                        (e.getY())>(cursorD[0].y-40)&&
                        (e.getY())<(cursorD[0].y+40);
                if(in_Point_A){
                    cursorA[0] =new Point(e.getX(),e.getY());
                }
                if(in_Point_B){
                    cursorB[0] =new Point(e.getX(),e.getY());
                }
                if(in_Point_C){
                    cursorC[0] =new Point(e.getX(),e.getY());
                }
                if(in_Point_D){
                    cursorD[0] =new Point(e.getX(),e.getY());
                }
//                for (int i = 0; i < defects.size(); i++) {
//                    Imgproc.circle(source1[0], new Point(defects.elementAt(i).x,defects.elementAt(i).y),defects.elementAt(i).radius, new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
//                   // Imgproc.circle(black[0], new Point(defects.elementAt(i).x,defects.elementAt(i).y),defects.elementAt(i).radius, new Scalar(0, 255, 255));
//                }
                List<Point> listSrcs= Arrays.asList(fourPoints.elementAt(0),fourPoints.elementAt(1),fourPoints.elementAt(2),fourPoints.elementAt(3));
                List<Point> listDsts = Arrays.asList(cursorA[0],cursorB[0],cursorC[0],cursorD[0]);
                Mat srcPoints = Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);
                Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
                Mat perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
                Imgproc.warpPerspective(source3[0], dst, perspectiveMmat, source3[0].size(), Imgproc.INTER_LINEAR);
                BufferedImage image4 = mat2BufferedImage.matToBufferedImage(dst);
                label.setIcon(new ImageIcon(image4));
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        };
        // 图形变换的鼠标移动监听 M3_2
        MouseMotionListener M3_2 = new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {

                boolean in_Point_A=(e.getX())>(cursorA[0].x-40)&&
                        (e.getX())<(cursorA[0].x+40)&&
                        (e.getY())>(cursorA[0].y-40)&&
                        (e.getY())<(cursorA[0].y+40);
                boolean in_Point_B=(e.getX())>(cursorB[0].x-40)&&
                        (e.getX())<(cursorB[0].x+40)&&
                        (e.getY())>(cursorB[0].y-40)&&
                        (e.getY())<(cursorB[0].y+40);
                boolean in_Point_C=(e.getX())>(cursorC[0].x-40)&&
                        (e.getX())<(cursorC[0].x+40)&&
                        (e.getY())>(cursorC[0].y-40)&&
                        (e.getY())<(cursorC[0].y+40);
                boolean in_Point_D=(e.getX())>(cursorD[0].x-40)&&
                        (e.getX())<(cursorD[0].x+40)&&
                        (e.getY())>(cursorD[0].y-40)&&
                        (e.getY())<(cursorD[0].y+40);
                if(in_Point_A){
                    cursorA[0] =new Point(e.getX(),e.getY());
                }
                if(in_Point_B){
                    cursorB[0] =new Point(e.getX(),e.getY());
                }
                if(in_Point_C){
                    cursorC[0] =new Point(e.getX(),e.getY());
                }
                if(in_Point_D){
                    cursorD[0] =new Point(e.getX(),e.getY());
                }
//                for (int i = 0; i < defects.size(); i++) {
//                    //Imgproc.circle(source3[0], new Point(defects.elementAt(i).x,defects.elementAt(i).y),defects.elementAt(i).radius, new Scalar(0, 255, 255));
//                    Imgproc.circle(black[0], new Point(defects.elementAt(i).x,defects.elementAt(i).y),defects.elementAt(i).radius, new Scalar(0, 255, 255),1,Imgproc.LINE_AA);
//                }
                List<Point> listSrcs= Arrays.asList(fourPoints.elementAt(0),fourPoints.elementAt(1),fourPoints.elementAt(2),fourPoints.elementAt(3));
                List<Point> listDsts = Arrays.asList(cursorA[0],cursorB[0],cursorC[0],cursorD[0]);
                Mat srcPoints = Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);
                Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
                Mat perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
                Imgproc.warpPerspective(black[0], dst, perspectiveMmat, black[0].size(), Imgproc.INTER_LINEAR);
                BufferedImage image4 = mat2BufferedImage.matToBufferedImage(dst);
                label.setIcon(new ImageIcon(image4));
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        };
        // 原图变换选项监听 A1
        ActionListener A1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(rb1.isSelected()) {
                    cursorA[0] = fourPoints.elementAt(0);
                    cursorB[0] = fourPoints.elementAt(1);
                    cursorC[0] = fourPoints.elementAt(2);
                    cursorD[0] = fourPoints.elementAt(3);
                    label.addMouseMotionListener(M3_1);
                }
            }
        };
        // 图形变换选项监听 A1
        ActionListener A2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(rb2.isSelected()) {
                    cursorA[0] = fourPoints.elementAt(0);
                    cursorB[0] = fourPoints.elementAt(1);
                    cursorC[0] = fourPoints.elementAt(2);
                    cursorD[0] = fourPoints.elementAt(3);
                    label.addMouseMotionListener(M3_2);
                }
            }
        };
        ActionListener A3 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(circle_shape.isSelected()){
                    label.removeMouseListener(M3);
                    label.removeMouseMotionListener(M2_3);
                    label.addMouseListener(M2);
                    label.addMouseMotionListener(M2_2);
                }
            }
        };
        ActionListener A4 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(poly_shape.isSelected()){
                    label.removeMouseListener(M2);
                    label.removeMouseMotionListener(M2_2);
                    label.addMouseListener(M3);
                    label.addMouseMotionListener(M2_3);
                }
            }
        };
        // 按钮监听事件
        // 读取图片
        ReadImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("读取图像");
                EnableTrans[0] =false;
                group.clearSelection();
                group2.clearSelection();
                rb1.setEnabled(false);
                rb2.setEnabled(false);
                fourPoints.removeAllElements();
                defects.removeAllElements();
                poly_defects_list.removeAllElements();
                label.removeMouseListener(M1);
                label.removeMouseListener(M2);
                label.removeMouseListener(M3);
                label.removeMouseMotionListener(M2_2);
                label.removeMouseMotionListener(M2_3);
                rb1.removeActionListener(A1);
                rb2.removeActionListener(A2);
                label.removeMouseMotionListener(M3_1);
                label.removeMouseMotionListener(M3_2);
                JLabel msg=new JLabel("是否加载默认图片？");
                msg.setFont(new Font("宋体",Font.BOLD,20));
                int result = JOptionPane.showConfirmDialog(
                        jf,
                        msg,
                        "提示",
                        JOptionPane.YES_NO_CANCEL_OPTION
                );
                System.out.println("选择结果: " + result);
                if(result==0){
                   source[0] = Imgcodecs.imread(".\\temp.bmp");
                   ImgHeight[0] = source[0].rows();
                   ImgWidth[0] = source[0].cols();
                   top[0] = (int) (0.5 * (h - 70) - 0.5 * ImgHeight[0]);
                   bottom[0] = (int) (0.5 * (h - 70) - 0.5 * ImgHeight[0]);
                   left[0] = (int) (0.5 * (w - ImgWidth[0]));
                   right[0] = (int) (0.5 * (w - ImgWidth[0]));
                    /*diff_x[0] =0.5*(w-ImgWidth[0]);
                    diff_y[0] =0.5*(h-ImgHeight[0])-toolBar.getHeight();*/
                   Core.copyMakeBorder(source[0], source[0], top[0], bottom[0], left[0], right[0], 0);
                   BufferedImage image = mat2BufferedImage.matToBufferedImage(source[0]);
                   label.setIcon(new ImageIcon(image));
                   FourPoints.setEnabled(true);
               }
               else if(result==1) {
                   JFileChooser fc = new JFileChooser("F:\\");
                   int val = fc.showOpenDialog(null);    //文件打开对话框
                   if (val == fc.APPROVE_OPTION) {
                       filename[0] = fc.getSelectedFile().toString();
                       source[0] = Imgcodecs.imread(filename[0]);
                       ImgHeight[0] = source[0].rows();
                       ImgWidth[0] = source[0].cols();
                       top[0] = (int) (0.5 * (h - 70) - 0.5 * ImgHeight[0]);
                       bottom[0] = (int) (0.5 * (h - 70) - 0.5 * ImgHeight[0]);
                       left[0] = (int) (0.5 * (w - ImgWidth[0]));
                       right[0] = (int) (0.5 * (w - ImgWidth[0]));
                    /*diff_x[0] =0.5*(w-ImgWidth[0]);
                    diff_y[0] =0.5*(h-ImgHeight[0])-toolBar.getHeight();*/
                       Core.copyMakeBorder(source[0], source[0], top[0], bottom[0], left[0], right[0], 0);
                       BufferedImage image = mat2BufferedImage.matToBufferedImage(source[0]);
                       label.setIcon(new ImageIcon(image));
                       FourPoints.setEnabled(true);
                   }
               }
            }
        });
        // 四点标定
        FourPoints.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FourPoints.setEnabled(false);
                ReadImage.setEnabled(false);
                CancelPoints.setEnabled(true);
                System.out.println("四点标定");
                label.addMouseListener(M1);
            }
        });
        // 取消标定
        CancelPoints.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CancelPoints.setEnabled(false);
                FourPoints.setEnabled(true);
                DefectLable.setEnabled(false);
                if(fourPoints.size()>0) {
                    label.removeMouseListener(M1);
                    System.out.println("撤销标定");
                    fourPoints.removeAllElements();
                    System.out.println(fourPoints.size());
                    //Mat source2 = Imgcodecs.imread(filename[0]);
                    //Core.copyMakeBorder(source[0],source[0], top[0], bottom[0], left[0], right[0],0);
                    BufferedImage image2 = mat2BufferedImage.matToBufferedImage(source[0]);
                    label.setIcon(new ImageIcon(image2));
                }
            }
        });
        // 缺陷标注
        DefectLable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EnableTrans[0]=true;
                ReadImage.setEnabled(false);
                CancelPoints.setEnabled(false);
                DefectLable.setEnabled(false);
                CancelLable.setEnabled(true);
                circle_shape.setSelected(false);
                poly_shape.setSelected(false);
                label.removeMouseListener(M1);
                System.out.println("缺陷标注");
                circle_shape.setEnabled(true);
                poly_shape.setEnabled(true);
                circle_shape.addActionListener(A3);
                poly_shape.addActionListener(A4);
            }
        });
        // 取消标注
        CancelLable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CancelLable.setEnabled(false);
                circle_shape.setEnabled(false);
                poly_shape.setEnabled(false);
                circle_shape.removeActionListener(A3);
                poly_shape.removeActionListener(A4);
                DefectLable.setEnabled(true);
                ReadImage.setEnabled(false);
                CancelPoints.setEnabled(false);
                label.removeMouseListener(M1);
                label.removeMouseListener(M2);
                label.removeMouseListener(M3);
                label.removeMouseMotionListener(M2_2);
                label.removeMouseMotionListener(M2_3);
                System.out.println("取消标注");
                group2.clearSelection();
                defects.removeAllElements();
                poly_defects_list.removeAllElements();
                enableLine[0]=0;
                BufferedImage image4 = mat2BufferedImage.matToBufferedImage(source1[0]);
                label.setIcon(new ImageIcon(image4));
            }
        });
        // 投影变换
        Transform.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rb1.setEnabled(true);
                if(EnableTrans[0]){
                    rb2.setEnabled(true);
                }
                else{
                    rb2.setEnabled(false);
                }
                CancelLable.setEnabled(false);
                DefectLable.setEnabled(false);
                ReadImage.setEnabled(true);
                FourPoints.setEnabled(false);
                CancelPoints.setEnabled(false);
                Transform.setEnabled(false);
                circle_shape.removeActionListener(A3);
                poly_shape.removeActionListener(A4);
                circle_shape.setEnabled(false);
                poly_shape.setEnabled(false);
                System.out.println("投影变换");
                label.removeMouseListener(M1);
                label.removeMouseListener(M2);
                label.removeMouseListener(M3);
                label.removeMouseMotionListener(M2_2);
                label.removeMouseMotionListener(M2_3);
                rb1.setSelected(false);
                rb2.setSelected(false);
                rb1.addActionListener(A1);
                rb2.addActionListener(A2);
            } //烫烫烫锟斤拷
        });
        // 添加 工具栏 到 内容面板 的 顶部
        panel.add(toolBar, BorderLayout.NORTH);
        jf.setContentPane(panel);
        // 界面显示
        jf.setVisible(true);

    }

}
