package com.br.main;

public class Aresta {

	private int custo;
	private int delay;
	private Vertice origem;
	private Vertice destino;

	public Aresta(Vertice v1, Vertice v2, int custo, int delay) {

		this.custo = custo;
		this.delay = delay;
		this.origem = v1;
		this.destino = v2;

	}

	public void setDestino(Vertice destino) {
		this.destino = destino;
	}

	public Vertice getDestino() {
		return destino;
	}

	public void setOrigem(Vertice origem) {
		this.origem = origem;
	}

	public Vertice getOrigem() {
		return origem;
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
