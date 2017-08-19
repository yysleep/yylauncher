package com.yanhuahealth.healthlauncher.sys;

import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
import com.yanhuahealth.healthlauncher.utils.DateTimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 电子书管理的单元测试用例
 */
public class EbookMgrTest {

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown\n");
        EbookMgr.getInstance().clearAllLocalEbooks();
    }

    @Test
    public void testGetAllLocalEbooks() throws Exception {
        System.out.println("testGetAllLocalEbooks");

        Ebook ebook = new Ebook();
        ebook.id = 102;
        ebook.catId = 2;
        ebook.name = "雪山飞狐";
        ebook.author = "金庸";
        ebook.publishTime = DateTimeUtils.getTimeStr(new Date());
        EbookMgr.getInstance().addEbook(ebook, true);

        List<Ebook> allLocalEbooks = EbookMgr.getInstance().getAllLocalEbooks();
        assertNotEquals(allLocalEbooks, null);
        assertEquals(allLocalEbooks.size(), 1);
        assertEquals(allLocalEbooks.get(0).id, 102);
        assertEquals(allLocalEbooks.get(0).catId, 2);
    }

    @Test
    public void testGetLocalEbooksWithCat() throws Exception {
        System.out.println("testGetLocalEbooksWithCat");

        Ebook ebook = new Ebook();
        ebook.id = 102;
        ebook.catId = 2;
        ebook.name = "雪山飞狐";
        ebook.author = "金庸";
        ebook.publishTime = DateTimeUtils.getTimeStr(new Date());
        EbookMgr.getInstance().addEbook(ebook, true);

        // 存在对应分类的电子书列表
        List<Ebook> catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(2);
        assertNotEquals(catEbooks, null);
        assertEquals(catEbooks.size(), 1);
        assertEquals(catEbooks.get(0).id, 102);
        assertEquals(catEbooks.get(0).catId, 2);

        // 其他分类的电子书列表
        List<Ebook> otherCatEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(1);
        assertEquals(otherCatEbooks, null);
    }

    @Test
    public void testAddEbook() throws Exception {
        System.out.println("testAddEbook");

        // 一本电子书
        Ebook ebook = new Ebook();
        ebook.id = 103;
        ebook.catId = 3;
        ebook.author = "古龙";
        ebook.name = "萧十一郎";
        EbookMgr.getInstance().addEbook(ebook, true);

        List<Ebook> allLocalEbooks = EbookMgr.getInstance().getAllLocalEbooks();
        assertNotEquals(allLocalEbooks, null);
        assertEquals(allLocalEbooks.size(), 1);

        Ebook firstEbook = allLocalEbooks.get(0);
        assertNotEquals(firstEbook, null);
        assertEquals(firstEbook.id, 103);
        assertEquals(firstEbook.catId, 3);

        List<Ebook> catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(3);
        assertNotEquals(catEbooks, null);
        assertEquals(catEbooks.size(), 1);

        Ebook ebookOfCat = catEbooks.get(0);
        assertNotEquals(ebookOfCat, null);
        assertEquals(ebookOfCat.id, 103);

        catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(1);
        assertEquals(catEbooks, null);

        // 再添加一本电子书到前面
        ebook = new Ebook();
        ebook.id = 102;
        ebook.catId = 2;
        ebook.name = "雪山飞狐";
        ebook.author = "金庸";
        ebook.publishTime = DateTimeUtils.getTimeStr(new Date());
        EbookMgr.getInstance().addEbook(ebook, true);

        allLocalEbooks = EbookMgr.getInstance().getAllLocalEbooks();
        assertNotEquals(allLocalEbooks, null);
        assertEquals(allLocalEbooks.size(), 2);

        firstEbook = allLocalEbooks.get(0);
        assertNotEquals(firstEbook, null);
        assertEquals(firstEbook.id, 102);
        assertEquals(firstEbook.catId, 2);

        Ebook secondEbook = allLocalEbooks.get(1);
        assertNotEquals(secondEbook, null);
        assertEquals(secondEbook.id, 103);
        assertEquals(secondEbook.catId, 3);

        catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(1);
        assertEquals(catEbooks, null);

        // 再添加一本电子书到尾部
        ebook = new Ebook();
        ebook.id = 104;
        ebook.catId = 4;
        ebook.name = "书剑恩仇录";
        ebook.author = "金庸";
        ebook.publishTime = DateTimeUtils.getTimeStr(new Date());
        EbookMgr.getInstance().addEbook(ebook, false);

        allLocalEbooks = EbookMgr.getInstance().getAllLocalEbooks();
        assertNotEquals(allLocalEbooks, null);
        assertEquals(allLocalEbooks.size(), 3);

        firstEbook = allLocalEbooks.get(0);
        assertNotEquals(firstEbook, null);
        assertEquals(firstEbook.id, 102);
        assertEquals(firstEbook.catId, 2);

        secondEbook = allLocalEbooks.get(1);
        assertNotEquals(secondEbook, null);
        assertEquals(secondEbook.id, 103);
        assertEquals(secondEbook.catId, 3);

        Ebook thirdEbook = allLocalEbooks.get(2);
        assertNotEquals(thirdEbook, null);
        assertEquals(thirdEbook.id, 104);
        assertEquals(thirdEbook.catId, 4);
    }

    @Test
    public void testRemoveEbook() throws Exception {
        System.out.println("testRemoveEbook");

        // 先添加一本电子书
        Ebook ebook = new Ebook();
        ebook.id = 103;
        ebook.catId = 3;
        ebook.author = "古龙";
        ebook.name = "萧十一郎";
        EbookMgr.getInstance().addEbook(ebook, true);

        // 移除一本
        EbookMgr.getInstance().removeEbook(ebook);

        // allLocalEbooks
        List<Ebook> allLocalEbooks = EbookMgr.getInstance().getAllLocalEbooks();
        assertNotEquals(allLocalEbooks, null);
        assertEquals(allLocalEbooks.size(), 0);

        // allLocalCatEbooks
        List<Ebook> catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(3);
        assertEquals(catEbooks, null);

        catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(2);
        assertEquals(catEbooks, null);

        // 添加两本电子书
        Ebook firstEbook = new Ebook();
        firstEbook.id = 103;
        firstEbook.catId = 3;
        firstEbook.author = "古龙";
        firstEbook.name = "萧十一郎";
        EbookMgr.getInstance().addEbook(firstEbook, true);

        Ebook secondEbook = new Ebook();
        secondEbook.id = 104;
        secondEbook.catId = 4;
        secondEbook.name = "书剑恩仇录";
        secondEbook.author = "金庸";
        secondEbook.publishTime = DateTimeUtils.getTimeStr(new Date());
        EbookMgr.getInstance().addEbook(secondEbook, false);

        // 移除第一本
        EbookMgr.getInstance().removeEbook(firstEbook);

        // allLocalEbooks
        allLocalEbooks = EbookMgr.getInstance().getAllLocalEbooks();
        assertNotEquals(allLocalEbooks, null);
        assertEquals(allLocalEbooks.size(), 1);

        // allLocalCatEbooks
        catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(3);
        assertEquals(catEbooks, null);

        catEbooks = EbookMgr.getInstance().getLocalEbooksWithCat(4);
        assertNotEquals(catEbooks, null);
        assertEquals(catEbooks.size(), 1);

        Ebook ebookOfCat = catEbooks.get(0);
        assertNotEquals(ebookOfCat, null);
        assertEquals(ebookOfCat.id, 104);
    }

    // 缩略图文件名匹配
    @Test
    public void testIsValidThumbFileName() throws Exception {
        System.out.println("testIsValidThumbFileName");

        // 有效电子书名
        String validThumbName = "128-1.jpg";
        assertTrue(EbookMgr.getInstance().isValidThumbFileName(validThumbName));

        validThumbName = "109239782-0.png";
        assertTrue(EbookMgr.getInstance().isValidThumbFileName(validThumbName));

        validThumbName = "/c/path/test/389232-1937.JPeg";
        assertTrue(EbookMgr.getInstance().isValidThumbFileName(validThumbName));

        // 无效电子书名
        String invalidThumbName = "invalid.pnga";
        assertFalse(EbookMgr.getInstance().isValidThumbFileName(invalidThumbName));

        invalidThumbName = "png";
        assertFalse(EbookMgr.getInstance().isValidThumbFileName(invalidThumbName));

        invalidThumbName = "1.jpeg";
        assertFalse(EbookMgr.getInstance().isValidThumbFileName(invalidThumbName));

        invalidThumbName = "1-2-3.jpeg";
        assertFalse(EbookMgr.getInstance().isValidThumbFileName(invalidThumbName));

        invalidThumbName = "/a/c/d/1-2.jpdg";
        assertFalse(EbookMgr.getInstance().isValidThumbFileName(invalidThumbName));
    }
}