package com.br.main;

public class Enlace {

	private int roteador1;
	private int roteador2;
	private int delay;
	private int custo;
	
	public Enlace(int roteador1, int roteador2, int custo, int delay) {
		this.roteador1 = roteador1;
		this.roteador2 = roteador2;
		this.custo = custo;
		this.delay = delay;
	}
	
	public int getRoteador1() {
		return roteador1;
	}
	public void setRoteador1(int roteador1) {
		this.roteador1 = roteador1;
	}
	public int getRoteador2() {
		return roteador2;
	}
	public void setRoteador2(int roteador2) {
		this.roteador2 = roteador2;
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
}
