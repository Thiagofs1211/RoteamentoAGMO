package com.br.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
	
	public static void main(String[] args) throws IOException {
		
		int origem = 1;
		int[] destinos = {2, 9, 10, 13, 14};
		int delayMax = 25;
		
		int quantidadePopulacao = 50;
		int numeroGeracoes = 50;
		int numeroFilhos = 20;
		
		List<Enlace> roteadores = lerArquivoRoteamento("Roteamento.txt");
		
		List<Arvore> populacao = geraPopulacaoInicial(origem, destinos, roteadores, quantidadePopulacao);
		for(Arvore arvore : populacao) {
			avaliaIndividuo(arvore, destinos, delayMax);
		}
		
		ag(populacao, numeroGeracoes, numeroFilhos, destinos, roteadores, delayMax, origem);
		System.out.println("FIM");
	}
	
	public static List<Enlace> lerArquivoRoteamento(String roteamento) throws IOException  {
		List<Enlace> roteadores = new ArrayList<>();
		BufferedReader buffRead = new BufferedReader(new FileReader(roteamento));
		String linha = buffRead.readLine();
		while(true) {
			if(linha != null) {
				String[] array = new String[4];
				array = linha.split(",");
				Enlace enlace = new Enlace(Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2]), Integer.parseInt(array[3]));
				roteadores.add(enlace);
			} else {
				break;
			}
			linha = buffRead.readLine();
		}
		buffRead.close();
		return roteadores;
	}
	
	public static List<Arvore> geraPopulacaoInicial(int origem, int[] destinos, List<Enlace> roteadores, int quantidadePopulacao) {
		List<Arvore> populacao = new ArrayList<Arvore>();
		Random rand = new Random();
		
		for(int i = 0; i < quantidadePopulacao; i++) {
			List<Integer> copiaDestinos = new ArrayList<Integer>();
			for(int j = 0; j < destinos.length; j++) {
				copiaDestinos.add(destinos[j]);
			}
			List<Integer> nosArvores = new ArrayList<Integer>();
			Arvore arvore = new Arvore(origem);
			nosArvores.add(origem);
			while(copiaDestinos.size() > 0) {
				int ramoSelecionado = rand.nextInt(nosArvores.size()); // Escolhe randomicamente o no a ser expandido na arvore
				List<Enlace> vizinhos = procuraVizinhos(roteadores, nosArvores.get(ramoSelecionado), nosArvores); // Recupera os nos vizinhos do no que sera expandido ignorando os nos que ja estão na arvore
				while(vizinhos == null || vizinhos.size() == 0) {
					ramoSelecionado = rand.nextInt(nosArvores.size());
					vizinhos = procuraVizinhos(roteadores, nosArvores.get(ramoSelecionado), nosArvores);
				}
				int novoNoSelecionado = rand.nextInt(vizinhos.size()); // Seleciona um novo no randomicamente
				
				//Adicionado o novo no na arvore
				No no = arvore.buscar(nosArvores.get(ramoSelecionado));
				if(vizinhos.get(novoNoSelecionado).getRoteador1() == nosArvores.get(ramoSelecionado)) {
					no.adicionaFilho(vizinhos.get(novoNoSelecionado).getRoteador2(), vizinhos.get(novoNoSelecionado).getCusto(), vizinhos.get(novoNoSelecionado).getDelay());
					//Verifica se atingiu algum nó destino
					if(isNoDestino(destinos, vizinhos.get(novoNoSelecionado).getRoteador2())) {
						for(int j = 0; j < copiaDestinos.size(); j++) {
							if(copiaDestinos.get(j) == vizinhos.get(novoNoSelecionado).getRoteador2()) {
								copiaDestinos.remove(j);
								break;
							}
						}
					}
					nosArvores.add(vizinhos.get(novoNoSelecionado).getRoteador2());
				} else {
					no.adicionaFilho(vizinhos.get(novoNoSelecionado).getRoteador1(), vizinhos.get(novoNoSelecionado).getCusto(), vizinhos.get(novoNoSelecionado).getDelay());
					//Verifica se atingiu algum nó destino
					if(isNoDestino(destinos, vizinhos.get(novoNoSelecionado).getRoteador1())) {
						for(int j = 0; j < copiaDestinos.size(); j++) {
							if(copiaDestinos.get(j) == vizinhos.get(novoNoSelecionado).getRoteador1()) {
								copiaDestinos.remove(j);
								break;
							}
						}
					}
					nosArvores.add(vizinhos.get(novoNoSelecionado).getRoteador1());
				}
			}
			arvore.getRaiz().limpaArvore(destinos);
			populacao.add(arvore);
		}
		
		return populacao;
	}
	
	public static void ag(List<Arvore> populacao, int numeroGeracoes, int numeroFilhos, int[] destinos, List<Enlace> enlaces, int delayMax, int origem) {
		for(int i = 0; i < numeroGeracoes; i++) {
			double maxRoleta = montarRoleta(populacao);
			int pai1 = roleta(populacao, maxRoleta);
			int pai2 = roleta(populacao, maxRoleta);
			
			while(pai1 == pai2) {
				pai2 = roleta(populacao, maxRoleta);
			}
			if(pai1 < 0 || pai2 < 0 || pai1 >= 50 || pai2 >= 50) {
				System.out.println("aqui");
			}
			
			crossover(populacao.get(pai1), populacao.get(pai2), destinos, enlaces, delayMax, origem);
		}
	}
	
	public static List<Enlace> procuraVizinhos(List<Enlace> roteadores, int valor, List<Integer> nosArvores) {
		List<Enlace> vizinhos = new ArrayList<Enlace>();
		List<Enlace> vizinhosNaoRepetidos = new ArrayList<Enlace>();
		for(Enlace roteador : roteadores) {
			if(roteador.getRoteador1() == valor || roteador.getRoteador2() == valor) {
				vizinhos.add(roteador);
			}
		}
		
		//Parte para verificar se algum vizinho já esta na arvore
		for(int i = 0; i < vizinhos.size(); i++) {
			if(!((vizinhos.get(i).getRoteador1() == valor && nosArvores.contains(vizinhos.get(i).getRoteador2())) || vizinhos.get(i).getRoteador2() == valor && nosArvores.contains(vizinhos.get(i).getRoteador1()))) {
				vizinhosNaoRepetidos.add(vizinhos.get(i));
			}
		}

		return vizinhosNaoRepetidos;
	}
	
	public static boolean isNoDestino(int[] nosDestinos, int valor) {
		for(int i = 0; i < nosDestinos.length; i++) {
			if(nosDestinos[i] == valor) {
				return true;
			}
		}
		return false;
	}
	
	public static void avaliaIndividuo(Arvore arvore, int[] destinos, int delayMax) {
		int custo = calculaCusto(arvore);
		double delay = calculaDelay(arvore, destinos, delayMax);
		arvore.setAptidao((1.0/custo) * delay);
	}
	
	public static int calculaCusto(Arvore arvore) {
		int soma = 0;
		for(No no : arvore.getRaiz().getFilhos()) {
			soma = soma + no.calculaCusto();
		}
		return soma;
	}
	
	public static double calculaDelay(Arvore arvore, int[] destinos, int delayMax) {
		double delay = 1;
		for(int i = 0; i < destinos.length; i++) {
			int soma = arvore.getRaiz().calculaDelay(destinos[i]);
			if(soma > delayMax) {
				delay = delay * 0.5;
			}
		}
		return delay;
	}
	
	public static double montarRoleta(List<Arvore> populacao) {
		double soma = 0;
		for(Arvore ind : populacao) {
			soma = soma + ind.getAptidao();
			ind.setCasaRoleta(soma);
		}
		return soma;
	}
	
	public static int roleta(List<Arvore> populacao, double maxRoleta) {
		double random = ThreadLocalRandom.current().nextDouble(0, maxRoleta);
		for(int i = 0; i < populacao.size(); i++) {
			if(populacao.get(i).getCasaRoleta() >= random) {
				return i;
			}
		}
		return -1;
	}
	
	public static Grafo montarGrafo(List<Enlace> enlaces) {
		Grafo grafo = new Grafo();
		List<Integer> verificados = new ArrayList<>();
		List<Vertice> vertices = new ArrayList<>();
		for(Enlace enlace : enlaces) {
			if(!verificados.contains(enlace.getRoteador1())) {
				Vertice v = new Vertice();
				v.setDescricao(String.valueOf(enlace.getRoteador1()));
				vertices.add(v);
				verificados.add(enlace.getRoteador1());
			}
			if(!verificados.contains(enlace.getRoteador2())) {
				Vertice v = new Vertice();
				v.setDescricao(String.valueOf(enlace.getRoteador2()));
				vertices.add(v);
				verificados.add(enlace.getRoteador2());
			}
		}
		for(Vertice vertice : vertices) {
			List<Vertice> vizinhos = new ArrayList<>();
			List<Aresta> arestas = new ArrayList<>();
			for(Enlace enlace : enlaces) {
				if(Integer.valueOf(vertice.getDescricao()) == enlace.getRoteador1()) {
					for(int i = 0; i < vertices.size(); i++) {
						if(Integer.valueOf(vertices.get(i).getDescricao()) == enlace.getRoteador2()) {
							vizinhos.add(vertices.get(i));
							Aresta aresta = new Aresta(vertice, vertices.get(i), enlace.getCusto(), enlace.getDelay());
							arestas.add(aresta);
						}
					}
				} else if(Integer.valueOf(vertice.getDescricao()) == enlace.getRoteador2()) {
					for(int i = 0; i < vertices.size(); i++) {
						if(Integer.valueOf(vertices.get(i).getDescricao()) == enlace.getRoteador1()) {
							vizinhos.add(vertices.get(i));
							Aresta aresta = new Aresta(vertice, vertices.get(i), enlace.getCusto(), enlace.getDelay());
							arestas.add(aresta);
						}
					}
				}
			}
			vertice.setVizinhos(vizinhos);
			vertice.setArestas(arestas);
		}
		grafo.setVertices(vertices);
		return grafo;
	}
	
	public static Arvore crossover(Arvore pai1, Arvore pai2, int[] destinos, List<Enlace> enlaces, int delayMax, int origem) {
		//Quebra da arvore verificando as rotas dos pais que são iguais, sem deixar nenhum ponto destino de fora
		List<Arvore> floresta = quebrarArvoreCrossover(pai1, pai2, destinos);
		Dijkstra dijkstra = new Dijkstra();
		Random rand = new Random();
		
		while(floresta.size() > 1) {
			int arvore1 = rand.nextInt(floresta.size());
			int arvore2 = rand.nextInt(floresta.size());
			while(arvore1 == arvore2) {
				arvore2 = rand.nextInt(floresta.size());
			}
			List<Adjacencia> adjacencia1 =  montarListaAdjacencias(floresta.get(arvore1).getRaiz(), new ArrayList<>(), new ArrayList<>());
			List<Adjacencia> adjacencia2 =  montarListaAdjacencias(floresta.get(arvore2).getRaiz(), new ArrayList<>(), new ArrayList<>());
			
			if(adjacencia1.get(0).getValor() > adjacencia2.get(0).getValor()) {
				List<Adjacencia> aux1 = adjacencia1;
				adjacencia1 = adjacencia2;
				adjacencia2 = aux1;
			}
			
			verificarRepeticao(adjacencia1, adjacencia2);
			
			System.out.println("Adjacencia 1: ");
			for(Adjacencia ad : adjacencia1) {
				System.out.print(ad.getValor()+": ");
				for(Integer aux : ad.getAdjacencias()) {
					System.out.print(aux+", ");
				}
				System.out.println();
			}
			System.out.println();
			
			System.out.println("Adjacencia 2: ");
			for(Adjacencia ad : adjacencia2) {
				System.out.print(ad.getValor()+": ");
				for(Integer aux : ad.getAdjacencias()) {
					System.out.print(aux+", ");
				}
				System.out.println();
			}
			System.out.println();
			
			if(!adjacencia2.isEmpty()) {
			
				Grafo grafoAux = montarGrafo(enlaces);
				List<Aresta> arestas = new ArrayList<Aresta>();
				List<Vertice> vizinhos = new ArrayList<Vertice>();
				
				//Criação do no virtual 1;
				Vertice aux1 = new Vertice();
				aux1.setDescricao("aux1");
				for(Adjacencia adjacencia: adjacencia1) {
					Vertice vertice = grafoAux.encontrarVertice(String.valueOf(adjacencia.getValor()));
					Aresta aresta1 = new Aresta(vertice, aux1, 0, 0);
					vertice.getArestas().add(aresta1);
					vertice.getVizinhos().add(aux1);
					
					Aresta aresta2 = new Aresta(aux1, vertice, 0, 0);
					arestas.add(aresta2);
					vizinhos.add(vertice);
				}
				aux1.setArestas(arestas);
				aux1.setVizinhos(vizinhos);
				arestas = new ArrayList<Aresta>();
				vizinhos = new ArrayList<Vertice>();
				
				//Criação do no virtual 2;
				Vertice aux2 = new Vertice();
				aux2.setDescricao("aux2");
				for(Adjacencia adjacencia: adjacencia2) {
					Vertice vertice = grafoAux.encontrarVertice(String.valueOf(adjacencia.getValor()));
					Aresta aresta1 = new Aresta(vertice, aux2, 0, 0);
					vertice.getArestas().add(aresta1);
					vertice.getVizinhos().add(aux2);
					
					Aresta aresta2 = new Aresta(aux2, vertice, 0, 0);
					arestas.add(aresta2);
					vizinhos.add(vertice);
				}
				aux2.setArestas(arestas);
				aux2.setVizinhos(vizinhos);
				
				grafoAux.adicionarVertice(aux1);
				grafoAux.adicionarVertice(aux2);
				
				List<Vertice> menorCaminho = new ArrayList<Vertice>();
				if(calculaDelay(pai1, destinos, delayMax) != 1.0) {
					menorCaminho= dijkstra.encontrarMenorCaminhoDijkstraDelay(grafoAux, aux1, aux2);
				} else {
					menorCaminho= dijkstra.encontrarMenorCaminhoDijkstraCusto(grafoAux, aux1, aux2);
				}
				System.out.print("Menor Caminho: ");
				for(Vertice v : menorCaminho) {
					System.out.print(v.getDescricao()+", ");
				}
				System.out.println();
				List<Adjacencia> fusao = juntarListasAdjacencias(adjacencia1, adjacencia2, menorCaminho, origem, grafoAux);
				if(arvore1 < arvore2) {
					floresta.remove(arvore2);
					floresta.remove(arvore1);
				} else {
					floresta.remove(arvore1);
					floresta.remove(arvore2);
				}
				floresta.add(adjacenteParaArvore(fusao));
			} else {
				if(arvore1 < arvore2) {
					floresta.remove(arvore2);
					floresta.remove(arvore1);
				} else {
					floresta.remove(arvore1);
					floresta.remove(arvore2);
				}
				floresta.add(adjacenteParaArvore(adjacencia1));
			}
		}
		return floresta.get(0);
	}
	
	public static void verificarRepeticao(List<Adjacencia> lista1, List<Adjacencia> lista2) {
		boolean repetido = false;
		Adjacencia adjacenteRepetido = null;
		List<Integer> listaRepetidos = new ArrayList<Integer>();
		List<Integer> verificados = new ArrayList<Integer>();
		for(Adjacencia ad1 : lista1) {
			for(Adjacencia ad2 : lista2) {
				if(ad1.getValor() == ad2.getValor()) {
					repetido = true;
					adjacenteRepetido = ad2;
					listaRepetidos.add(ad2.getValor());
					for(Integer aux : ad2.getAdjacencias()) {
						if(!verificaFilhosIguais(lista1, ad2.getAdjacencias())) {
							ad1.getAdjacencias().add(aux);
							if(!ad2.getAdjacencias().isEmpty()) {
								verificados.add(aux);
							}
						}
					}
				}
			}
			if(repetido) {
				lista2.remove(adjacenteRepetido);
				repetido = false;
			}
		}
		while(!verificados.isEmpty()) {
			for(Adjacencia ad2 : lista2) {
				if(ad2.getValor() == verificados.get(0)) {
					repetido = true;
					adjacenteRepetido = ad2;
					listaRepetidos.add(ad2.getValor());
					lista1.add(ad2);
					for(Integer aux : ad2.getAdjacencias()) {
						verificados.add(aux);
					}
				}
			}
			if(repetido) {
				lista2.remove(adjacenteRepetido);
				repetido = false;
			}
			verificados.remove(0);
		}
		while(!listaRepetidos.isEmpty()) {
			for(Adjacencia ad : lista2) {
				List<Integer> apagados = new ArrayList<>();
				for(int i = 0; i < ad.getAdjacencias().size(); i++) {
					if(ad.getAdjacencias().get(i) == listaRepetidos.get(0)) {
						apagados.add(i);
					}
				}
				for(int i = apagados.size(); i > 0; i--) {
					ad.getAdjacencias().remove(apagados.get(i-1));
				}
			}
			listaRepetidos.remove(0);
		}
		if(!lista2.isEmpty()) {
			List<Integer> excluir = new ArrayList<Integer>();
			for(int i = 0; i < lista2.get(0).getAdjacencias().size(); i++) {
				for(Adjacencia ad1 : lista1) {
					if(ad1.getValor() == lista2.get(0).getAdjacencias().get(i)) {
						excluir.add(i);
					}
				}
			}
			for(int i = excluir.size(); i > 0; i--) {
				lista2.get(0).getAdjacencias().remove(i-1);
			}
		}
	}
	
	public static boolean verificaFilhosIguais(List<Adjacencia> lista1, List<Integer> lista2) {
		for(Adjacencia ad1 : lista1) {
			for(Integer aux1 : ad1.getAdjacencias()) {
				for(Integer aux2 : lista2) {
					if(aux1 == aux2) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static List<Adjacencia> juntarListasAdjacencias(List<Adjacencia> adjacencia1, List<Adjacencia> adjacencia2, List<Vertice> menorCaminho, int origem, Grafo grafo) {
		List<Integer> nosNaoVerificados = new ArrayList<Integer>();
		List<Adjacencia> listaAdjacencia = new ArrayList<Adjacencia>();
		verificaRaizOrigem(adjacencia1, origem);
		//verificaRaiz(adjacencia1, adjacencia2, grafo);
		for(Adjacencia ad : adjacencia1) {
			listaAdjacencia.add(ad);
		}
		for(Adjacencia ad : adjacencia2) {
			nosNaoVerificados.add(ad.getValor());
		}
		for(int i = 0; i < menorCaminho.size(); i++) {
			Adjacencia aux = null;
			for(Adjacencia ad : adjacencia1) {
				if(menorCaminho.get(i).getDescricao().equals(String.valueOf(ad.getValor()))) {
					if(i != menorCaminho.size()-1) {
						if("aux1".equals(menorCaminho.get(i+1).getDescricao()) && !"aux2".equals(menorCaminho.get(i+2).getDescricao())) {
							if(ad.getAdjacencias() == null) {
								ad.setAdjacencias(new ArrayList<Integer>());
							}
							ad.getAdjacencias().add(Integer.valueOf(menorCaminho.get(i+2).getDescricao()));
							aux = new Adjacencia();
							aux.setValor(Integer.valueOf(menorCaminho.get(i+2).getDescricao()));
							listaAdjacencia.add(aux);
						} else if("aux2".equals(menorCaminho.get(i+1).getDescricao())) {
							for(Adjacencia ad2: adjacencia2) {
								if(menorCaminho.get(i+2).getDescricao().equals(String.valueOf(ad2.getValor()))) {
									if(ad2.getAdjacencias() == null || ad2.getAdjacencias().isEmpty()) {
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getValor()) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
										if(ad.getAdjacencias() == null) {
											ad.setAdjacencias(new ArrayList<Integer>());
										}
										if(nosNaoVerificados.isEmpty()) {
											ad.getAdjacencias().add(ad2.getValor());
											aux = new Adjacencia();
											aux.setValor(ad2.getValor());
											aux.setAdjacencias(ad2.getAdjacencias());
											listaAdjacencia.add(aux);
										} else {
											for(Adjacencia ad3 : adjacencia2) {
												for(int j = 0; j < ad3.getAdjacencias().size(); j++) {
													if(ad3.getAdjacencias().get(j) == ad2.getValor()) {
														aux = new Adjacencia();
														aux.setValor(ad3.getAdjacencias().get(j));
														aux.setAdjacencias(new ArrayList<Integer>());
														aux.getAdjacencias().add(ad3.getValor());
														ad3.getAdjacencias().remove(j);
														break;
													}
												}
											}
										}
									} else {
										if(ad.getAdjacencias() == null || ad2.getAdjacencias().isEmpty()) {
											ad.setAdjacencias(new ArrayList<>());
										}
										ad.getAdjacencias().add(ad2.getValor());
										aux = new Adjacencia();
										aux.setValor(ad2.getValor());
										aux.setAdjacencias(ad2.getAdjacencias());
										listaAdjacencia.add(aux);
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getValor()) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
									}
								}
							}
							while(!nosNaoVerificados.isEmpty()) {
								for(Adjacencia ad2: adjacencia2) {
									if(nosNaoVerificados.contains(ad2.getValor())) {
										if(ad2.getAdjacencias() == null || ad2.getAdjacencias().isEmpty()) {
											if(ad.getAdjacencias() == null) {
												ad.setAdjacencias(new ArrayList<Integer>());											
											}
											if(nosNaoVerificados.isEmpty()) {
												ad.getAdjacencias().add(ad2.getValor());
												aux = new Adjacencia();
												aux.setValor(ad2.getValor());
												aux.setAdjacencias(ad2.getAdjacencias());
												listaAdjacencia.add(aux);
												for(int j = 0; j < nosNaoVerificados.size(); j++) {
													if(nosNaoVerificados.get(j) == ad2.getValor()) {
														nosNaoVerificados.remove(j);
														break;
													}
												}
											} else {
												for(Adjacencia ad3 : adjacencia2) {
													for(int j = 0; j < ad3.getAdjacencias().size(); j++) {
														if(ad3.getAdjacencias().get(j) == ad2.getValor()) {
															aux = new Adjacencia();
															aux.setValor(ad3.getAdjacencias().get(j));
															aux.setAdjacencias(new ArrayList<Integer>());
															aux.getAdjacencias().add(ad3.getValor());
															ad3.getAdjacencias().remove(j);
															break;
														}
													}
												}
											}
										} else {
											aux = new Adjacencia();
											aux.setValor(ad2.getValor());
											aux.setAdjacencias(ad2.getAdjacencias());
											listaAdjacencia.add(aux);
											for(int j = 0; j < nosNaoVerificados.size(); j++) {
												if(nosNaoVerificados.get(j) == ad2.getValor()) {
													nosNaoVerificados.remove(j);
													break;
												}
											}
										}
									}
								}
							}
						} else if("aux1".equals(menorCaminho.get(i+1).getDescricao()) && ("aux2".equals(menorCaminho.get(i+2).getDescricao()))) {
							for(Adjacencia ad2: adjacencia2) {
								if(menorCaminho.get(i+3).getDescricao().equals(String.valueOf(ad2.getValor()))) {
									if(ad2.getAdjacencias() == null || ad2.getAdjacencias().isEmpty()) {
										if(ad.getAdjacencias() == null) {
											ad.setAdjacencias(new ArrayList<Integer>());											
										}
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getValor()) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
										if(nosNaoVerificados.isEmpty()) {
											ad.getAdjacencias().add(ad2.getValor());
											aux = new Adjacencia();
											aux.setValor(ad2.getValor());
											aux.setAdjacencias(ad2.getAdjacencias());
											listaAdjacencia.add(aux);
										} else {
											for(Adjacencia ad3 : adjacencia2) {
												for(int j = 0; j < ad3.getAdjacencias().size(); j++) {
													if(ad3.getAdjacencias().get(j) == ad2.getValor()) {
														aux = new Adjacencia();
														aux.setValor(ad3.getAdjacencias().get(j));
														aux.setAdjacencias(new ArrayList<Integer>());
														aux.getAdjacencias().add(ad3.getValor());
														ad3.getAdjacencias().remove(j);
														break;
													}
												}
											}
										}
									} else {
										ad.getAdjacencias().add(ad2.getValor());
										aux = new Adjacencia();
										aux.setValor(ad2.getValor());
										aux.setAdjacencias(ad2.getAdjacencias());
										listaAdjacencia.add(aux);
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getValor()) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
									}
								}
							}
							while(!nosNaoVerificados.isEmpty()) {
								for(Adjacencia ad2: adjacencia2) {
									if(nosNaoVerificados.contains(ad2.getValor())) {
										if(ad2.getAdjacencias() == null || ad2.getAdjacencias().isEmpty()) {
											if(ad.getAdjacencias() == null) {
												ad.setAdjacencias(new ArrayList<Integer>());
											}
											if(nosNaoVerificados.isEmpty()) {
												ad.getAdjacencias().add(ad2.getValor());
												aux = new Adjacencia();
												aux.setValor(ad2.getValor());
												aux.setAdjacencias(ad2.getAdjacencias());
												listaAdjacencia.add(aux);
												for(int j = 0; j < nosNaoVerificados.size(); j++) {
													if(nosNaoVerificados.get(j) == ad2.getValor()) {
														nosNaoVerificados.remove(j);
														break;
													}
												}
											} else {
												for(Adjacencia ad3 : adjacencia2) {
													for(int j = 0; j < ad3.getAdjacencias().size(); j++) {
														if(ad3.getAdjacencias().get(j) == ad2.getValor()) {
															aux = new Adjacencia();
															aux.setValor(ad3.getAdjacencias().get(j));
															aux.setAdjacencias(new ArrayList<Integer>());
															aux.getAdjacencias().add(ad3.getValor());
															ad3.getAdjacencias().remove(j);
															break;
														}
													}
												}
											}
										} else {
											aux = new Adjacencia();
											aux.setValor(ad2.getValor());
											aux.setAdjacencias(ad2.getAdjacencias());
											listaAdjacencia.add(aux);
											for(int j = 0; j < nosNaoVerificados.size(); j++) {
												if(nosNaoVerificados.get(j) == ad2.getValor()) {
													nosNaoVerificados.remove(j);
													break;
												}
											}
										}
									}
								}
							}
						} else {
							if(ad.getAdjacencias() == null) {
								ad.setAdjacencias(new ArrayList<Integer>());
							}
							ad.getAdjacencias().add(Integer.valueOf(menorCaminho.get(i+1).getDescricao()));
							aux = new Adjacencia();
							aux.setValor(Integer.valueOf(menorCaminho.get(i+1).getDescricao()));
							listaAdjacencia.add(aux);
						}
					}
				}
			}
			if(aux != null) {
				adjacencia1.add(aux);
			}
		}
		return listaAdjacencia;
	}
	
	public static void verificaRaizOrigem(List<Adjacencia> lista, int origem) {
		if(origem != lista.get(0).getValor()) {
			for(Adjacencia ad : lista) {
				if(ad.getValor() == origem) {
					for(int i = 0; i < lista.size(); i++) {
						for(int j = 0; j < lista.get(i).getAdjacencias().size(); j++) {
							if(lista.get(i).getAdjacencias().get(j) == origem) {
								lista.get(i).getAdjacencias().remove(j);
								Adjacencia aux = lista.get(i);
								lista.remove(i);
								lista.add(aux);
							}
						}
					}
				}
			}
		}
	}
	
	public static void verificaRaiz(List<Adjacencia> lista1, List<Adjacencia> lista2, Grafo grafo) {
		for(Vertice v : grafo.getVertices()){
			if(!"aux1".equals(v.getDescricao()) && !"aux2".equals(v.getDescricao()) && lista1.get(0).getValor() == Integer.valueOf(v.getDescricao())) {
				for(Vertice v2 : v.getVizinhos()) {
					if(!"aux1".equals(v2.getDescricao()) && !"aux2".equals(v2.getDescricao()) && !lista2.isEmpty() && Integer.valueOf(v2.getDescricao()) == lista2.get(0).getValor()) {
						lista1.get(0).getAdjacencias().add(lista2.get(0).getValor());
						if(lista2.get(0).getAdjacencias().isEmpty()) {
							lista2.remove(0);
						}
					}
				}
			}
		}
	}
	
	public static List<Arvore> quebrarArvoreCrossover(Arvore pai1, Arvore pai2, int[] destinos) {
		List<Arvore> floresta = new ArrayList<>();
		List<Adjacencia> adjacenciaPai1 = montarListaAdjacencias(pai1.getRaiz(), new ArrayList<>(), new ArrayList<>());
		List<Adjacencia> adjacenciaPai2 = montarListaAdjacencias(pai2.getRaiz(), new ArrayList<>(), new ArrayList<>());
		List<Integer> verificados = new ArrayList<Integer>();
		for(Adjacencia ad1 : adjacenciaPai1) {
			for(Adjacencia ad2 : adjacenciaPai2) {
				if(ad1.getValor() == ad2.getValor() && !verificados.contains(ad1.getValor())) {
					verificados.add(ad1.getValor());
					List<Integer> naoVerificados = new ArrayList<Integer>();
					List<Adjacencia> listaAdjacencia = new ArrayList<Adjacencia>();
					Adjacencia adjacencia = new Adjacencia();
					adjacencia.setValor(ad1.getValor());
					adjacencia.setAdjacencias(new ArrayList<Integer>());
					if(ad1.getAdjacencias() != null) {
						for(Integer aux1 : ad1.getAdjacencias()) {
							if(ad2.getAdjacencias() != null) {
								for(Integer aux2 : ad2.getAdjacencias()) {
									if(aux1 == aux2) {
										adjacencia.getAdjacencias().add(aux1);
										naoVerificados.add(aux1);
										verificados.add(aux1);
										break;
									}
								}
							}
						}
					}
					if(!adjacencia.getAdjacencias().isEmpty()) {
						listaAdjacencia.add(adjacencia);
					}
					while(!naoVerificados.isEmpty()) {
						for(Adjacencia aux1 : adjacenciaPai1) {
 							if(aux1.getValor() == naoVerificados.get(0)) {
								for(Adjacencia aux2 : adjacenciaPai2) {
									if(aux1.getValor() == aux2.getValor()) {
										adjacencia = new Adjacencia();
										adjacencia.setValor(aux1.getValor());
										adjacencia.setAdjacencias(new ArrayList<Integer>());
										if(aux1.getAdjacencias() != null) {
											for(Integer aux3 : aux1.getAdjacencias()) {
												if(aux2.getAdjacencias() != null) {
													for(Integer aux4 : aux2.getAdjacencias()) {
														if(aux3 == aux4) {
															adjacencia.getAdjacencias().add(aux3);
															naoVerificados.add(aux3);
															verificados.add(aux3);
															break;
														}
													}
												}
											}
										}
										listaAdjacencia.add(adjacencia);
									}
								}
							}
						}
						naoVerificados.remove(0);
					}
					if(!listaAdjacencia.isEmpty()) {
						floresta.add(adjacenteParaArvore(listaAdjacencia));
					}
				}
			}
		}
		for(int i = 0; i < destinos.length; i++) {
			boolean achou = false;
			for(Arvore arvore : floresta) {
				if(arvore.buscar(destinos[i]) != null) {
					achou = true;
				}
			}
			if(!achou) {
				Arvore arvore = new Arvore(destinos[i]);
				floresta.add(arvore);
			}
		}
		return floresta;
	}
	
	public static Arvore adjacenteParaArvore(List<Adjacencia> adjacentes) {
		Arvore arvore = new Arvore(adjacentes.get(0).getValor());
		List<Integer> faltaAdicionar = new ArrayList<Integer>();
		for(Adjacencia ad : adjacentes) {
			faltaAdicionar.add(ad.getValor());
		}
		while(!faltaAdicionar.isEmpty()) {
			No no = arvore.buscar(faltaAdicionar.get(0));
			if(no == null) {
				int aux = faltaAdicionar.get(0);
				faltaAdicionar.remove(0);
				faltaAdicionar.add(aux);
			} else {
				for(Adjacencia ad : adjacentes) {
					if(ad.getValor() == faltaAdicionar.get(0)) {
						for(Integer aux : ad.getAdjacencias()) {
							no.adicionaFilho(aux, -1, -1);
						}
					}
				}
				faltaAdicionar.remove(0);
			}
		}
		return arvore;
	}
	
	public static List<Adjacencia> montarListaAdjacencias(No no, List<Adjacencia> listaAdjacencia, List<Integer> verificados) {

		if(!verificados.contains(no.getValor())) {
			verificados.add(no.getValor());
			listaAdjacencia.add(recuperaAdjacencias(no));
			
			for(No aux : no.getFilhos()) {
				montarListaAdjacencias(aux, listaAdjacencia, verificados);
			}
		}
		
		return listaAdjacencia;
	}
	
	public static Adjacencia recuperaAdjacencias(No no) {
		Adjacencia adjacencia = new Adjacencia();
		adjacencia.setValor(no.getValor());
		if(no.getFilhos().size() == 0) {
			adjacencia.setAdjacencias(new ArrayList<Integer>());
			return adjacencia;
		}
		List<Integer> adjacente = new ArrayList<Integer>();
		for(No aux :  no.getFilhos()) {
			adjacente.add(aux.getValor());
		}
		adjacencia.setAdjacencias(adjacente);
		return adjacencia;
	}

}
