package com.br.main;

public class Arvore implements Comparable<Arvore> {
	private No raiz;
	private double aptidao;
	private double casaRoleta;
	
	public Arvore(int valorRaiz) {
		this.raiz = new No(valorRaiz,0,0);
	}
	
	public No buscar(int procurado) {
		return raiz.buscar(procurado);
	}

	public No getRaiz() {
		return raiz;
	}

	public void setRaiz(No raiz) {
		this.raiz = raiz;
	}

	public double getAptidao() {
		return aptidao;
	}

	public void setAptidao(double aptidao) {
		this.aptidao = aptidao;
	}

	public double getCasaRoleta() {
		return casaRoleta;
	}

	public void setCasaRoleta(double casaRoleta) {
		this.casaRoleta = casaRoleta;
	}
	
	public void preencheDelayCustoNos(Grafo grafo) {
		this.raiz.setDelay(0);
		this.raiz.setCusto(0);
		for(No no : this.raiz.getFilhos()) {
			no.preencheDelayCustoNo(grafo, this.getRaiz());
		}
	}
	
	@Override public int compareTo(Arvore arvore) { 
		if (this.aptidao < arvore.getAptidao()) { 
		  return -1; 
		  } if (this.aptidao > arvore.getAptidao()) { 
		  return 1; 
		  } 
		  return 0; 
		 }
}
