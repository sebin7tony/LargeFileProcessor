package com.tor.file;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class Heap {

	private List<Node> input;
	private int curSize = 0;
	
	public Heap() {
		
		input = new ArrayList<Node>();
	}

    public List<Node> getInput() {
		return input;
	}

	public boolean isEmpty() {
		return input.isEmpty();
	}

    public void addToHeap(Node node) {

		if(node.getValue() != null) {
			input.add(node);
			int parent = curSize;
			int child;

			do {
				child = parent;
				parent = (child - 1) / 2;
				if ((input.get(parent)).getValue().compareTo(input.get(child).getValue()) > 0) {
					swapNodes(parent, child);
				}
			} while (parent >= 1);
			curSize++;
		}
	}

	public Node getMin(){
		if(input.isEmpty())
			return null;
		Node min = input.get(0);
		input.set(0, input.get(curSize-1));
		input.remove(curSize-1);
		curSize--;
		heapify(0);
		return min;
	}
	
	public void heapify(int i){

		if(input.isEmpty())
			return ;
		int l = 2*i+1;
		int r = 2*i+2;
		int small = i;
		if( (l < curSize  && input.get(l).getValue().compareTo(input.get(small).getValue()) < 0)){
			small = l;
		}
		
		if( r < curSize && input.get(r).getValue().compareTo(input.get(small).getValue()) < 0){
			small = r;
		}
		
		if(!input.get(small).getValue().equals(input.get(i).getValue())){
			swapNodes(i,small);
			heapify(small);
		} 
	}

	private void swapNodes(int parent, int child) {
		Node tmp = input.get(parent);
		input.set(parent, input.get(child));
		input.set(child, tmp);
	}

	public class Node{

		private String value;
		private BufferedReader curBuffer;

		public Node(String value,BufferedReader buf){
			this.value = value;
			this.curBuffer = buf;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public void setCurBuffer(BufferedReader curBuffer) {
			this.curBuffer = curBuffer;
		}

		public String getValue() {
			return value;
		}

		public BufferedReader getCurBuffer() {
			return curBuffer;
		}
	}
}
