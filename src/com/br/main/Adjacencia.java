package com.br.main;

import java.util.List;

public class Adjacencia implements Comparable<Adjacencia> {
	
	private int valor;
	private List<Integer> adjacencias;
	
	public int getValor() {
		return valor;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}
	public List<Integer> getAdjacencias() {
		return adjacencias;
	}
	public void setAdjacencias(List<Integer> adjacencias) {
		this.adjacencias = adjacencias;
	}
	
	@Override 
	public int compareTo(Adjacencia adjacencia) { 
		if (this.valor > adjacencia.getValor()) { 
			return 1; 
		} if (this.valor < adjacencia.getValor()) { 
			return -1; 
		} 
			return 0; 
		}

}
