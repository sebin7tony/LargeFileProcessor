package com.tor.file;

import org.junit.Assert;
import org.junit.Test;

public class HeapSortTest {

    String[] input = {"hello","python","java","world"};


    @Test
    public void addToHeap() {
        Heap heap = new Heap();

        Heap.Node n1 = heap.new Node("hello",null);
        Heap.Node n2 = heap.new Node("world",null);
        Heap.Node n3 = heap.new Node("java",null);
        Heap.Node n4 = heap.new Node("python",null);
        heap.addToHeap(n1);
        heap.addToHeap(n2);
        heap.addToHeap(n3);
        heap.addToHeap(n4);
        Assert.assertEquals("world",heap.getInput().get(3).getValue());
    }

    @Test
    public void getMin() {
        Heap heap = new Heap();

        Heap.Node n1 = heap.new Node("hello",null);
        Heap.Node n2 = heap.new Node("world",null);
        Heap.Node n3 = heap.new Node("java",null);
        Heap.Node n4 = heap.new Node("python",null);
        heap.addToHeap(n1);
        heap.addToHeap(n2);
        heap.addToHeap(n3);
        heap.addToHeap(n4);
        Assert.assertEquals("hello",heap.getMin().getValue());
        Assert.assertEquals("java",heap.getMin().getValue());
        Assert.assertEquals("python",heap.getMin().getValue());
        Assert.assertEquals("world",heap.getMin().getValue());
        Assert.assertNull(heap.getMin());
    }
}
