/**
 * Created by xymeow on 16/4/30.
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.lang.*;
import java.util.*;
import java.text.DecimalFormat;


public class MyCacheSim extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JPanel panelTop, panelLeft, panelRight, panelBottom;
    private JButton execStepBtn, execAllBtn, fileBotton, restarBtn;
    private JComboBox csBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
    private JComboBox icsBox, dcsBox;
    private JFileChooser fileChooser;

    private JLabel labelTop, labelLeft, rightLabel, bottomLabel, fileLabel, fileAddrBtn, stepLabel1, stepLabel2, csLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel;
    private JLabel icsLabel, dcsLabel;
    private JLabel resultTagLabel[][];
    private JLabel resultDataLabel[][];

    private JLabel accessTypeTagLabel, addressTagLabel, blockNumberTagLabel, tagTagLabel, indexTagLabel, inblockAddressTagLabel, hitTagLabel;
    private JLabel accessTypeDataLabel, addressDataLabel, blockNumberDataLabel, tagDataLabel, indexDataLabel, inblockAddressDataLabel, hitDataLabel;

    private JRadioButton unifiedCacheButton, separateCacheButton;

    private final String cachesize[] = {"2KB", "8KB", "32KB", "128KB", "512KB", "2MB"};
    private final String scachesize[] = {"1KB", "4KB", "16KB", "64KB", "256KB", "1MB"};
    private final String blocksize[] = {"16B", "32B", "64B", "128B", "256B"};
    private final String way[] = {"直接映象", "2路", "4路", "8路", "16路", "32路"};
    private final String replace[] = {"LRU", "FIFO", "RAND"};
    private final String pref[] = {"不预取", "不命中预取"};
    private final String write[] = {"写回法", "写直达法"};
    private final String alloc[] = {"按写分配", "不按写分配"};
    private final  String typename[] = { "读数据", "写数据", "读指令" };

    private final String resultTags[][] = {
            {"访问总次数:", "不命中次数:", "不命中率:"},
            {"读指令次数:", "不命中次数:", "不命中率:"},
            {"读数据次数:", "不命中次数:", "不命中率:"},
            {"写数据次数:", "不命中次数:", "不命中率:"}
    };


    private File file;

    private int csIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex, instcsIndex, datacsIndex;
    private final int MAX_INSTRUCTION = 10000000;

    private int cacheType = 0;

    final static char[] digits = {'0','1'};
    private static String toBinaryString(int i) {
        char[] buf = new char[32];
        int pos = 32;
        int mask = 1;
        do {
            buf[--pos] = digits[i & mask];
            i >>>= 1;
        } while (pos > 0);

        return new String(buf, pos, 32);
    }


    private class Instruction {
        int type;
        int tag;
        int index;
        int blockAddr;
        int offset;
        String addr;

        public Instruction(int type, String addr) {
            this.type = type;
            this.addr = addr;

            int tmpp = Integer.parseInt(addr, 16);
            String baddr = toBinaryString(tmpp);
            //System.out.println(baddr);

            if (cacheType == 0 && uCache != null) {
                this.tag = Integer.parseInt(baddr.substring(0, 32 - uCache.blockOffset - uCache.setOffset), 2);
                this.index = Integer.parseInt(baddr.substring(32 - uCache.blockOffset - uCache.setOffset, 32 - uCache.blockOffset), 2);
                this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - uCache.blockOffset), 2);
                this.offset = Integer.parseInt(baddr.substring(32 - uCache.blockOffset), 2);
            }
            if (cacheType == 1 && instCache != null && dataCache != null) {
                if (type == 0 || type == 1) {
                    this.tag = Integer.parseInt(baddr.substring(0, 32 - dataCache.blockOffset - dataCache.setOffset), 2);
                    this.index = Integer.parseInt(baddr.substring(32 - dataCache.blockOffset - dataCache.setOffset, 32 - dataCache.blockOffset), 2);
                    this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - dataCache.blockOffset), 2);
                    this.offset = Integer.parseInt(baddr.substring(32 - dataCache.blockOffset), 2);
                } else if (type == 2) {
                    this.tag = Integer.parseInt(baddr.substring(0, 32 - instCache.blockOffset - instCache.setOffset), 2);
                    this.index = Integer.parseInt(baddr.substring(32 - instCache.blockOffset - instCache.setOffset, 32 - instCache.blockOffset), 2);
                    this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - instCache.blockOffset), 2);
                    this.offset = Integer.parseInt(baddr.substring(32 - instCache.blockOffset), 2);
                }
            }
        }
    }

    private Instruction instructions[];
    private int isize;
    private int pc;

    /**
     * CACHE BLOCK CLASS
     */

    private class CacheBlock {
        int tag;
        boolean dirty;
        int usedtime;
        int reachtime;

        public CacheBlock(int tag) {
            this.tag = tag;
            dirty = false;
            usedtime = -1;
            reachtime = -1;
        }
    }


    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    /**
        cache class
    */

    private class Cache {

        private CacheBlock cache[][];
        private int cacheSize;
        private int blockSize;
        private int blockNum;
        private int blockOffset;
        private int blockPerSet;
        private int setNum;
        private int setOffset;

        public Cache(int csize, int bsize) {
            cacheSize = csize;
            blockSize = bsize;

            blockNum = cacheSize / blockSize;
            blockOffset = log2(blockSize);

            blockPerSet = 1<<wayIndex;
            setNum = blockNum / blockPerSet;
            setOffset = log2(setNum);

            cache = new CacheBlock[setNum][blockPerSet];

            for (int i = 0; i < setNum; i++) {
                for (int j = 0; j < blockPerSet; j++) {
                    cache[i][j] = new CacheBlock(-1);
                }
            }
        }

        public boolean read(int tag, int index) {
            for (int i = 0; i < blockPerSet; i++) {
//                System.out.println(tag+" "+cache[index][i].tag);
                if (cache[index][i].tag == tag) {//hit
                    cache[index][i].usedtime = pc%isize;
                    return true;
                }
            }
            return false;
        }

        public boolean write(int tag, int index) {
            for (int i = 0; i < blockPerSet; i++) {
                if (cache[index][i].tag == tag) {//hit
                    cache[index][i].usedtime = pc%isize;
                    if (writeIndex == 0) {//write back
                        cache[index][i].dirty = true;
                    } else if (writeIndex == 1) {//write through
                        memoryWriteTime++;
                    }
                    return true;
                }
            }
            return false;
        }

        public void prefetch(int nextBlockAddr) {

            int nextTag = nextBlockAddr / (1<<setOffset);
            int nextIndex = nextBlockAddr / (1<<blockOffset) % (1<<setOffset);

            replace(nextTag, nextIndex);
        }

        public void replace(int tag, int index) {
            int toReplace = 0;
            if (replaceIndex == 0) {//LRU
                for (int i = 1; i < blockPerSet; i++) {
                    if (cache[index][toReplace].usedtime > cache[index][i].usedtime) {
                        toReplace = i;
                    }
                }
            } else if (replaceIndex == 1) {//FIFO
                for (int i = 1; i < blockPerSet; i++) {
                    if (cache[index][toReplace].reachtime > cache[index][i].reachtime) {
                        toReplace = i;
                    }
                }
            } else if (replaceIndex == 2) {//random
                toReplace = (int)Math.random()*blockPerSet;
            }
            loadBlock(tag, index, toReplace);
        }

        private void loadBlock(int tag, int index, int offset) {
            if (writeIndex == 0 && cache[index][offset].dirty) {
                //write back before being replaced;
                memoryWriteTime++;
            }

            cache[index][offset].tag = tag;
            cache[index][offset].usedtime = pc%isize;
            cache[index][offset].dirty = false;
            cache[index][offset].reachtime = pc%isize;
        }

        public void description() {
            System.out.println("cacheSize = " + cacheSize);
            System.out.println("blockSize = " + blockSize);
            System.out.println("blockNum = " + blockNum);
            System.out.println("blockOffset = " + blockOffset);
            System.out.println("blockPerSet = " + blockPerSet);
            System.out.println("setNum = " + setNum);
            System.out.println("setOffset = " + setOffset);
        }
    }

    Cache uCache, instCache, dataCache;


    private int readDataMissTime, readInstMissTime, readInstHitTime, readDataHitTime;
    private int writeDataHitTime, writeDataMissTime;
    private int memoryWriteTime;


    private class DinFileFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            String name = f.getName();
            return name.endsWith(".din") || name.endsWith(".DIN");
        }

        public String getDescription() {
            return ".din";
        }
    }


    public MyCacheSim() {
        super("Cache Simulator");
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new DinFileFilter());
        draw();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == execAllBtn) {
            simExecAll();
        }
        if (e.getSource() == execStepBtn) {
            simExecStep(true);
        }
        if (e.getSource() == restarBtn) {
            initCache();
            readFile();
            reloadUI();
        }
        if (e.getSource() == fileBotton) {
            int fileOver = fileChooser.showOpenDialog(null);
            if (fileOver == 0) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                fileAddrBtn.setText(path);
                file = new File(path);
                initCache();
                readFile();
                reloadUI();
            }
        }
    }


    private void initCache() {

        readDataMissTime = 0;
        readInstMissTime = 0;
        readDataHitTime = 0;
        readInstHitTime = 0;
        writeDataHitTime = 0;
        writeDataMissTime = 0;
        memoryWriteTime = 0;

        if (cacheType == 0) {
            uCache = new Cache(1024*(1<<(2*csIndex+1)), 1<<(bsIndex+4));
            instCache = null;
            dataCache = null;

            System.out.println("Unified Cache:");
            uCache.description();

        } else if (cacheType == 1) {
            uCache = null;
            instCache = new Cache(1024*(1<<(2*instcsIndex)), 1<<(bsIndex+4));
            dataCache = new Cache(1024*(1<<(2*datacsIndex)), 1<<(bsIndex+4));

            System.out.println("Instruction Cache:");
            instCache.description();
            System.out.println("Data Cache:");
            dataCache.description();
        }
    }


    private void readFile() {
        try {
            Scanner s = new Scanner(file);
            instructions = new Instruction[MAX_INSTRUCTION];
            isize = 0;
            pc = 0;

            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] items = line.split(" ");
                instructions[isize] = new Instruction(Integer.parseInt(items[0].trim()), items[1].trim());
                isize++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                resultDataLabel[i][j].setText("0");
            }
            resultDataLabel[i][2].setText("0.00%");
        }

        accessTypeDataLabel.setText("--");
        addressDataLabel.setText("--");
        blockNumberDataLabel.setText("--");
        tagDataLabel.setText("--");
        indexDataLabel.setText("--");
        inblockAddressDataLabel.setText("--");
        hitDataLabel.setText("--");
    }

    private void simExecStep(boolean oneStepExec) {
        pc %= isize;
        if (pc == 0) {
            initCache();
            reloadUI();
        }
        int type = instructions[pc].type;
        int index = instructions[pc].index;
        int tag = instructions[pc].tag;
        int offset = instructions[pc].offset;


        boolean isHit = false;
        if (cacheType == 0) { // UNION CACHE
            if (type == 0) {// read data
                isHit = uCache.read(tag, index);
                if (isHit) {
                    readDataHitTime++;
                } else {
                    readDataMissTime++;
                    uCache.replace(tag, index);
                }
            } else if (type == 1) {// write data
                isHit = uCache.write(tag, index);
                if (isHit) {
                    writeDataHitTime++;
                } else {
                    writeDataMissTime++;
                    if (allocIndex == 0) {//write alloc
                        uCache.replace(tag, index);
                        uCache.write(tag, index);
                    } else if (allocIndex == 1) {//no write alloc
                        memoryWriteTime++;
                    }
                }


            } else if (type == 2) {// read instruction
                isHit = uCache.read(tag, index);
                if (isHit) {
                    readInstHitTime++;
                } else {
                    readInstMissTime++;
                    uCache.replace(tag, index);
                    if (prefetchIndex == 0) {// do not prefetch
                        ;
                    } else if (prefetchIndex == 1) {// prefetch if instruction miss
                        uCache.prefetch(instructions[pc].blockAddr + 1);
                    }
                }
            }

        } else if (cacheType == 1) { //saperate cache
            if (type == 0) {// read data
                isHit = dataCache.read(tag, index);
                if (isHit) {
                    readDataHitTime++;
                } else {
                    readDataMissTime++;
                    dataCache.replace(tag, index);
                }
            } else if (type == 1) {// write data
                isHit = dataCache.write(tag, index);
                if (isHit) {
                    writeDataHitTime++;
                } else {
                    writeDataMissTime++;
                    if (allocIndex == 0) {//write alloc
                        dataCache.replace(tag, index);
                        dataCache.write(tag, index);
                    } else if (allocIndex == 1) {//no write alloc
                        memoryWriteTime++;
                    }
                }


            } else if (type == 2) {// read instruction
                isHit = instCache.read(tag, index);
                if (isHit) {
                    readInstHitTime++;
                } else {
                    readInstMissTime++;
                    instCache.replace(tag, index);
                    if (prefetchIndex == 0) {// do not prefetch
                        ;
                    } else if (prefetchIndex == 1) {// prefetch if instruction missed!
                        instCache.prefetch(instructions[pc].blockAddr + 1);
                    }
                }
            }
        }

        if (oneStepExec || pc == isize - 1) {
            updateGUI(instructions[pc], isHit);
        }
        pc++;
    }

    DecimalFormat df = new DecimalFormat("0.00%");

    private void updateGUI(Instruction inst, boolean hit) {

        int totalMissTime = readInstMissTime + readDataMissTime + writeDataMissTime;
        int totalVisitTime = totalMissTime + readInstHitTime + readDataHitTime + writeDataHitTime;
        double missRate;
        resultDataLabel[0][0].setText(totalVisitTime + "");
        resultDataLabel[0][1].setText(totalMissTime + "");
        if (totalVisitTime > 0) {
            missRate = ((double) totalMissTime / (double) totalVisitTime);
            resultDataLabel[0][2].setText(df.format(missRate));
        }

        resultDataLabel[1][0].setText((readInstHitTime + readInstMissTime) + "");
        resultDataLabel[1][1].setText(readInstMissTime + "");
        if (readInstMissTime + readInstHitTime > 0) {
            missRate = ((double) readInstMissTime / (double) (readInstMissTime + readInstHitTime));
            resultDataLabel[1][2].setText(df.format(missRate));
        }

        resultDataLabel[2][0].setText((readDataHitTime + readDataMissTime) + "");
        resultDataLabel[2][1].setText(readDataMissTime + "");
        if (readDataMissTime + readDataHitTime > 0) {
            missRate = ((double) readDataMissTime / (double) (readDataMissTime + readDataHitTime));
            resultDataLabel[2][2].setText(df.format(missRate));
        }

        resultDataLabel[3][0].setText((writeDataHitTime + writeDataMissTime) + "");
        resultDataLabel[3][1].setText(writeDataMissTime + "");
        if (writeDataMissTime + writeDataHitTime > 0) {
            missRate = ((double) writeDataMissTime / (double) (writeDataMissTime + writeDataHitTime));
            resultDataLabel[3][2].setText(df.format(missRate));
        }
        accessTypeDataLabel.setText(typename[inst.type]);
        addressDataLabel.setText(inst.addr);
        blockNumberDataLabel.setText(inst.blockAddr + "");
        tagDataLabel.setText(inst.tag + "");
        indexDataLabel.setText(inst.index + "");
        inblockAddressDataLabel.setText(inst.offset + "");

        if (hit) {
            hitDataLabel.setText("命中");
        } else {
            hitDataLabel.setText("未命中");
        }
    }


    private void simExecAll() {
        while (pc < isize) {
            simExecStep(false);
        }
        System.out.println(isize);
    }


    private void unifiedCacheEnabled(boolean enabled) {
        unifiedCacheButton.setSelected(enabled);
        csLabel.setEnabled(enabled);
        csBox.setEnabled(enabled);
    }

    private void separateCacheEnabled(boolean enabled) {
        separateCacheButton.setSelected(enabled);
        icsLabel.setEnabled(enabled);
        dcsLabel.setEnabled(enabled);
        icsBox.setEnabled(enabled);
        dcsBox.setEnabled(enabled);
    }

    private void draw() {
        setLayout(new BorderLayout(5, 5));
        panelTop = new JPanel();
        panelLeft = new JPanel();
        panelRight = new JPanel();
        panelBottom = new JPanel();
        panelTop.setPreferredSize(new Dimension(800, 50));
        panelLeft.setPreferredSize(new Dimension(300, 450));
        panelRight.setPreferredSize(new Dimension(500, 450));
        panelBottom.setPreferredSize(new Dimension(800, 100));
        panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        labelTop = new JLabel("made by xymeow");
        labelTop.setAlignmentX(CENTER_ALIGNMENT);
        panelTop.add(labelTop);


        labelLeft = new JLabel("Cache 参数设置");
        labelLeft.setPreferredSize(new Dimension(300, 40));

        csLabel = new JLabel("总大小");
        csLabel.setPreferredSize(new Dimension(80, 30));
        csBox = new JComboBox(cachesize);
        csBox.setPreferredSize(new Dimension(90, 30));
        csBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                csIndex = csBox.getSelectedIndex();
            }
        });
        //cache 种类
        unifiedCacheButton = new JRadioButton("统一Cache:", true);
        unifiedCacheButton.setPreferredSize(new Dimension(100, 30));
        unifiedCacheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                separateCacheEnabled(false);
                unifiedCacheEnabled(true);
                cacheType = 0;
            }
        });

        separateCacheButton = new JRadioButton("分离Cache:");
        separateCacheButton.setPreferredSize(new Dimension(100, 30));
        separateCacheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                separateCacheEnabled(true);
                unifiedCacheEnabled(false);
                cacheType = 1;
            }
        });

        icsLabel = new JLabel("指令Cache");
        icsLabel.setPreferredSize(new Dimension(80, 30));

        dcsLabel = new JLabel("数据Cache");
        dcsLabel.setPreferredSize(new Dimension(80, 30));

        JLabel emptyLabel = new JLabel("");
        emptyLabel.setPreferredSize(new Dimension(100, 30));

        icsBox = new JComboBox(scachesize);
        icsBox.setPreferredSize(new Dimension(90, 30));
        icsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                instcsIndex = icsBox.getSelectedIndex();
            }
        });

        dcsBox = new JComboBox(scachesize);
        dcsBox.setPreferredSize(new Dimension(90, 30));
        dcsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                datacsIndex = dcsBox.getSelectedIndex();
            }
        });

        separateCacheEnabled(false);
        unifiedCacheEnabled(true);

        //cache 块大小设置
        bsLabel = new JLabel("块大小");
        bsLabel.setPreferredSize(new Dimension(120, 30));
        bsBox = new JComboBox(blocksize);
        bsBox.setPreferredSize(new Dimension(160, 30));
        bsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                bsIndex = bsBox.getSelectedIndex();
            }
        });

        //相连度设置
        wayLabel = new JLabel("相联度");
        wayLabel.setPreferredSize(new Dimension(120, 30));
        wayBox = new JComboBox(way);
        wayBox.setPreferredSize(new Dimension(160, 30));
        wayBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                wayIndex = wayBox.getSelectedIndex();
            }
        });

        //替换策略设置
        replaceLabel = new JLabel("替换策略");
        replaceLabel.setPreferredSize(new Dimension(120, 30));
        replaceBox = new JComboBox(replace);
        replaceBox.setPreferredSize(new Dimension(160, 30));
        replaceBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                replaceIndex = replaceBox.getSelectedIndex();
            }
        });

        //欲取策略设置
        prefetchLabel = new JLabel("预取策略");
        prefetchLabel.setPreferredSize(new Dimension(120, 30));
        prefetchBox = new JComboBox(pref);
        prefetchBox.setPreferredSize(new Dimension(160, 30));
        prefetchBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                prefetchIndex = prefetchBox.getSelectedIndex();
            }
        });

        //写策略设置
        writeLabel = new JLabel("写策略");
        writeLabel.setPreferredSize(new Dimension(120, 30));
        writeBox = new JComboBox(write);
        writeBox.setPreferredSize(new Dimension(160, 30));
        writeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                writeIndex = writeBox.getSelectedIndex();
            }
        });

        //调块策略
        allocLabel = new JLabel("写不命中调块策略");
        allocLabel.setPreferredSize(new Dimension(120, 30));
        allocBox = new JComboBox(alloc);
        allocBox.setPreferredSize(new Dimension(160, 30));
        allocBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                allocIndex = allocBox.getSelectedIndex();
            }
        });

        //选择指令流文件
        fileLabel = new JLabel("选择指令流文件");
        fileLabel.setPreferredSize(new Dimension(120, 30));
        fileAddrBtn = new JLabel();
        fileAddrBtn.setPreferredSize(new Dimension(210, 30));
        fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        fileBotton = new JButton("浏览");
        fileBotton.setPreferredSize(new Dimension(70, 30));
        fileBotton.addActionListener(this);

        panelLeft.add(labelLeft);

        panelLeft.add(unifiedCacheButton);
        panelLeft.add(csLabel);
        panelLeft.add(csBox);

        panelLeft.add(separateCacheButton);
        panelLeft.add(icsLabel);
        panelLeft.add(icsBox);
        panelLeft.add(emptyLabel);
        panelLeft.add(dcsLabel);
        panelLeft.add(dcsBox);

        panelLeft.add(bsLabel);
        panelLeft.add(bsBox);
        panelLeft.add(wayLabel);
        panelLeft.add(wayBox);
        panelLeft.add(replaceLabel);
        panelLeft.add(replaceBox);
        panelLeft.add(prefetchLabel);
        panelLeft.add(prefetchBox);
        panelLeft.add(writeLabel);
        panelLeft.add(writeBox);
        panelLeft.add(allocLabel);
        panelLeft.add(allocBox);
        panelLeft.add(fileLabel);
        panelLeft.add(fileAddrBtn);
        panelLeft.add(fileBotton);

        //*****************************右侧面板绘制*****************************************//
        //模拟结果展示区域
        rightLabel = new JLabel("模拟结果");
        rightLabel.setPreferredSize(new Dimension(500, 40));
        panelRight.add(rightLabel);

        resultTagLabel = new JLabel[4][3];
        resultDataLabel = new JLabel[4][3];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                resultTagLabel[i][j] = new JLabel(resultTags[i][j]);
                resultTagLabel[i][j].setPreferredSize(new Dimension(70, 40));

                if (j != 2) {
                    resultDataLabel[i][j] = new JLabel("0");
                } else {
                    resultDataLabel[i][j] = new JLabel("0.00%");
                }

                resultDataLabel[i][j].setPreferredSize(new Dimension(83, 40));

                panelRight.add(resultTagLabel[i][j]);
                panelRight.add(resultDataLabel[i][j]);
            }
            if (i == 0) {
                JLabel label = new JLabel("其中:");
                label.setPreferredSize(new Dimension(500, 40));
                panelRight.add(label);
            }
        }

        accessTypeTagLabel = new JLabel("访问类型:");
        addressTagLabel = new JLabel("地址:");
        blockNumberTagLabel = new JLabel("块号:");
        tagTagLabel = new JLabel("标记Tag:");
        indexTagLabel = new JLabel("组索引:");
        inblockAddressTagLabel = new JLabel("块内地址:");
        hitTagLabel = new JLabel("命中情况:");

        accessTypeDataLabel = new JLabel("--");
        addressDataLabel = new JLabel("--");
        blockNumberDataLabel = new JLabel("--");
        tagDataLabel = new JLabel("--");
        indexDataLabel = new JLabel("--");
        inblockAddressDataLabel = new JLabel("--");
        hitDataLabel = new JLabel("--");

        accessTypeTagLabel.setPreferredSize(new Dimension(80, 40));
        accessTypeDataLabel.setPreferredSize(new Dimension(80, 40));
        addressTagLabel.setPreferredSize(new Dimension(80, 40));
        addressDataLabel.setPreferredSize(new Dimension(200, 40));
        panelRight.add(accessTypeTagLabel);
        panelRight.add(accessTypeDataLabel);
        panelRight.add(addressTagLabel);
        panelRight.add(addressDataLabel);

        blockNumberTagLabel.setPreferredSize(new Dimension(80, 40));
        blockNumberDataLabel.setPreferredSize(new Dimension(200, 40));
        hitTagLabel.setPreferredSize(new Dimension(80, 40));
        hitDataLabel.setPreferredSize(new Dimension(80, 40));
        panelRight.add(blockNumberTagLabel);
        panelRight.add(blockNumberDataLabel);
        panelRight.add(hitTagLabel);
        panelRight.add(hitDataLabel);

        tagTagLabel.setPreferredSize(new Dimension(60, 40));
        tagDataLabel.setPreferredSize(new Dimension(70, 40));
        indexTagLabel.setPreferredSize(new Dimension(60, 40));
        indexDataLabel.setPreferredSize(new Dimension(70, 40));
        inblockAddressTagLabel.setPreferredSize(new Dimension(60, 40));
        inblockAddressDataLabel.setPreferredSize(new Dimension(100, 40));
        panelRight.add(tagTagLabel);
        panelRight.add(tagDataLabel);
        panelRight.add(indexTagLabel);
        panelRight.add(indexDataLabel);
        panelRight.add(inblockAddressTagLabel);
        panelRight.add(inblockAddressDataLabel);


        //*****************************底部面板绘制*****************************************//

        bottomLabel = new JLabel("执行控制");
        bottomLabel.setPreferredSize(new Dimension(800, 30));
        execStepBtn = new JButton("步进");
        execStepBtn.setLocation(100, 30);
        execStepBtn.addActionListener(this);
        execAllBtn = new JButton("执行到底");
        execAllBtn.setLocation(300, 30);
        execAllBtn.addActionListener(this);
        restarBtn = new JButton("复位");
        restarBtn.setLocation(500, 30);
        restarBtn.addActionListener(this);


        panelBottom.add(bottomLabel);
        panelBottom.add(execStepBtn);
        panelBottom.add(execAllBtn);
        panelBottom.add(restarBtn);

        add("North", panelTop);
        add("West", panelLeft);
        add("Center", panelRight);
        add("South", panelBottom);
        setSize(820, 620);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new MyCacheSim();
    }
}