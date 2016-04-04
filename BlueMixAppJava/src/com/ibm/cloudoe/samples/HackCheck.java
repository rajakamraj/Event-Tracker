package com.ibm.cloudoe.samples;

import java.util.Scanner;

public class HackCheck {
	 public static void main(String[] args) {
	        /* Enter your code here. Read input from STDIN. Print output to STDOUT. Your class should be named Solution. */
	        Scanner in =new Scanner(System.in);
	        System.out.println("Enter the size of the array");
	        int N=in.nextInt();
	        int sum=0;
	        int[] intArray=new int[N];
	        for(int i=0;i<N;i++)
	            {
	            intArray[i]=in.nextInt();
	            System.out.println();
	            sum=sum + intArray[i];
	        }
	        
	        System.out.println("The sum of the entered integer array is "+ sum);
	       
	        
	    }
}
