package com.br.main;

import java.util.ArrayList;
import java.util.List;

public class No {
	private int valor;
	private int custo;
	private int delay;
	private List<No> filhos;
	
	public No(int valor, int custo, int delay) {
		this.valor = valor;
		this.custo = custo;
		this.delay = delay;
		filhos = new ArrayList<No>();
	}
	
	public No adicionaFilho(int valor, int custo, int delay) {
		No no = new No(valor, custo, delay);
		filhos.add(no);
		return no;
	}
	
	public No buscar(int procurado) {
		if(procurado == valor) return this;
		for(No filho: filhos) {
			No achou = filho.buscar(procurado);
			if(achou != null) return achou;
		}
		return null;
	}
	
	public boolean isNoFolha() {
		if(this.filhos == null || this.filhos.size() == 0) {
			return true;
		}
		return false;
	}
	
	public boolean isDestino(int[] destinos) {
		for(int i = 0; i < destinos.length; i++) {
			if(destinos[i] == this.valor) {
				return true;
			}
		}
		return false;
	}
	
	public boolean limpaArvore(int[] destinos) {
		List<No> nosApagados = new ArrayList<No>();
		boolean apagou = false;
		for(No no : this.filhos) {
			if(no.isNoFolha()) {
				if(!no.isDestino(destinos)) {
					nosApagados.add(no);
				}
			} else {
				if(no.limpaArvore(destinos)) {
					if(no.isNoFolha()) {
						if(!no.isDestino(destinos)) {
							nosApagados.add(no);
						}
					}
				}
			}
		}
		for(No no : nosApagados) {
			apagou = true;
			this.filhos.remove(no);
		}
		return apagou;
	}
	
	public int calculaCusto() {
		int soma = 0;
		soma = soma + this.custo;
		for(No no : this.filhos) {
			soma = soma + no.calculaCusto();
		}
		if(this.filhos.isEmpty()) {
			return this.custo;
		}
		return soma;
	}
	
	public int calculaDelay(int valorBuscado) {
		int soma = 0;
		if(this.valor == valorBuscado) {
			return this.delay;
		}
		for(No no: this.filhos) {
			if(no.buscar(valorBuscado) != null) {
				soma = soma + this.delay + no.calculaDelay(valorBuscado);
			}
		}
		return soma;
	}
	
	public void preencheDelayCustoNo(Grafo grafo, No antecessor) {
		for(Vertice v : grafo.getVertices()) {
			if(Integer.valueOf(v.getDescricao()) == antecessor.getValor()) {
				for(Aresta a : v.getArestas()) {
					if(Integer.valueOf(a.getDestino().getDescricao()) == this.getValor()) {
						this.setCusto(a.getCusto());
						this.setDelay(a.getDelay());
						break;
					}
				}
			}
		}
		for(No no : this.getFilhos()) {
			no.preencheDelayCustoNo(grafo, this);
		}
	}
	
	public int getValor() {
		return valor;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}
	public List<No> getFilhos() {
		return filhos;
	}
	public void setFilhos(List<No> filhos) {
		this.filhos = filhos;
	}
	public int getCusto() {
		return custo;
	}
	public void setCusto(int custo) {
		this.custo = custo;
	}
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
}
