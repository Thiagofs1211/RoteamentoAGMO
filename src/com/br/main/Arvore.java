package com.br.main;

public class Arvore {
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
}
