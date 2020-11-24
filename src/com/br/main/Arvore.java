package com.br.main;

public class Arvore implements Comparable<Arvore> {
	private No raiz;
	private double aptidao;
	private double casaRoleta;
	private int custo;
	private int delay;
	private double funcao1;
	private double funcao2;
	private double funcao3;
	private int fronteira;
	private double crowdingDistance;
	private boolean naoDominado = false;
	private int numeroDominados;
	private int strengths = 0;
	private double densidade = 0;
	
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

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getCusto() {
		return custo;
	}

	public void setCusto(int custo) {
		this.custo = custo;
	}

	public double getFuncao1() {
		return funcao1;
	}

	public void setFuncao1(double funcao1) {
		this.funcao1 = funcao1;
	}

	public double getFuncao2() {
		return funcao2;
	}

	public void setFuncao2(double funcao2) {
		this.funcao2 = funcao2;
	}

	public double getFuncao3() {
		return funcao3;
	}

	public void setFuncao3(double funcao3) {
		this.funcao3 = funcao3;
	}

	public int getFronteira() {
		return fronteira;
	}

	public void setFronteira(int fronteira) {
		this.fronteira = fronteira;
	}

	public double getCrowdingDistance() {
		return crowdingDistance;
	}

	public void setCrowdingDistance(double crowdingDistance) {
		this.crowdingDistance = crowdingDistance;
	}

	public boolean isNaoDominado() {
		return naoDominado;
	}

	public void setNaoDominado(boolean naoDominado) {
		this.naoDominado = naoDominado;
	}

	public int getNumeroDominados() {
		return numeroDominados;
	}

	public void setNumeroDominados(int numeroDominados) {
		this.numeroDominados = numeroDominados;
	}

	public int getStrengths() {
		return strengths;
	}

	public void setStrengths(int strengths) {
		this.strengths = strengths;
	}

	public double getDensidade() {
		return densidade;
	}

	public void setDensidade(double densidade) {
		this.densidade = densidade;
	}
}
