package com.br.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
	
	public static void main(String[] args) throws IOException {
		
		int origem = 1;
		
		// Rede 0
		//int[] destinos = {2, 9, 10, 13, 14};
		//int delayMax = 25;
		//List<Enlace> roteadores = lerArquivoRoteamento("Roteamento.txt");
		
		// Rede 1
		//List<Enlace> roteadores = lerArquivoRoteamento("Rede1.txt");
		//int[] destinos = {7, 11, 14, 16, 18};
		//int delayMax = 9;
		
		//Rede Mista
		List<Enlace> roteadores = lerArquivoRoteamento("RedeMista.txt");
		int[] destinos = {2, 9, 10, 13, 14, 22, 26, 29, 31, 33};
		int delayMax = 25;
		
		
		int quantidadePopulacao = 50;
		int numeroGeracoes = 50;
		int numeroFilhos = 20;
		int taxaMutacao = 2;
		
		//String metodo = "spea2";
		//String metodo = "nsga2";
		String metodo = "agSimples";
		
		
		List<Arvore> populacao = geraPopulacaoInicial(origem, destinos, roteadores, quantidadePopulacao);
		if("agSimples".equals(metodo)) {
			for(Arvore arvore : populacao) {
				avaliaIndividuo(arvore, destinos, delayMax);
			}
		}
		
		boolean[] funcoes = {true, false, false, true};
		
		long start = System.currentTimeMillis();
		
		populacao = ag(populacao, numeroGeracoes, numeroFilhos, destinos, roteadores, delayMax, origem, taxaMutacao, funcoes, metodo, quantidadePopulacao);
		
		long elapsed = System.currentTimeMillis() - start;
		
		if("agSimples".equals(metodo)) {
			
			Collections.sort(populacao);
			
			List<Adjacencia> melhor = montarListaAdjacencias(populacao.get(0).getRaiz(), new ArrayList<>(), new ArrayList<>());
			
			System.out.println("Melhor Individuo: ");
			for(Adjacencia ad : melhor) {
				System.out.print(ad.getValor()+": ");
				for(Integer aux : ad.getAdjacencias()) {
					System.out.print(aux+", ");
				}
				System.out.println();
			}
			System.out.println();
			System.out.println("Custo: " + calculaCusto(populacao.get(0)));
			System.out.println("Delay: " + delay(populacao.get(0), destinos));
			System.out.println("Tempo: " + elapsed/1000.0);
		} else {
			System.out.println("Ótimo Pareto:");
			mostraOtimoPareto(populacao, metodo);
			
			System.out.println("\nError Rate: " + errorRate(populacao, metodo));
			System.out.println("Pareto Subset: " + paretoSubset(populacao, metodo));
			
		}
		
		
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
	
	public static List<Arvore> ag(List<Arvore> populacao, int numeroGeracoes, int numeroFilhos, int[] destinos, List<Enlace> enlaces, int delayMax, int origem, int taxaMutacao, boolean funcoes[], String metodo, int quantidadePopulacao) {
		Grafo grafo = montarGrafo(enlaces);
		for(int i = 0; i < numeroGeracoes; i++) {
			System.out.println(i);
			List<Arvore> filhos = new ArrayList<Arvore>();
			
			if("nsga2".equals(metodo)) {
				nsga2(populacao, destinos, funcoes);
			}
			if("spea2".equals(metodo)) {
				spea2(populacao, funcoes, destinos);
			}
			
			for(int j = 0; j < numeroFilhos; j++) {
				int pai1 = -1;
				int pai2 = -1;
				if("agSimples".equals(metodo)) {
					double maxRoleta = montarRoleta(populacao);
					pai1 = roleta(populacao, maxRoleta);
					pai2 = roleta(populacao, maxRoleta);
					
					while(pai1 == pai2) {
						pai2 = roleta(populacao, maxRoleta);
					}
				} else if("nsga2".equals(metodo)) {
					pai1 = torneio2nsga2(populacao);
					pai2 = torneio2nsga2(populacao);
					
					while(pai1 == pai2) {
						pai2 = torneio2nsga2(populacao);
					}
				} else if("spea2".equals(metodo)) {
					pai1 = torneio3(populacao);
					pai2 = torneio3(populacao);
					
					while(pai1 == pai2) {
						pai2 = torneio3(populacao);
					}
				}
				
				Arvore arvore = new Arvore(0);
				arvore = crossover(populacao.get(pai1), populacao.get(pai2), destinos, enlaces, delayMax, origem);
				arvore.preencheDelayCustoNos(grafo);
				
				Random rand = new Random();
				int chanceMutacao = rand.nextInt(100);
				if(chanceMutacao < taxaMutacao) {
					arvore = mutacao(arvore, destinos, enlaces, delayMax, origem);
					arvore.preencheDelayCustoNos(grafo);
					filhos.add(arvore);
				} else {
					filhos.add(arvore);
				}
			}
			if("agSimples".equals(metodo)) {
				for(Arvore ar : filhos) {
					avaliaIndividuo(ar, destinos, delayMax);
				}
				Collections.sort(populacao);
				for(int j = populacao.size(); j > populacao.size()/2; j--) {
					populacao.remove(j-1);
				}
			}
			for(int j = 0; j < filhos.size(); j++) {
				populacao.add(filhos.get(j));
			}
			if("nsga2".equals(metodo)) {
				nsga2(populacao, destinos, funcoes);
				
				Collections.sort(populacao, new Comparator<Arvore>(){
			        public int compare(Arvore first, Arvore second){
			        	if (first.getFronteira() < second.getFronteira()) { 
			      		  return -1; 
			      		  } if (first.getFronteira() > second.getFronteira()) { 
			      		  return 1; 
			      		  } 
			      		  return 0; 
			      		 }
			        });
				populacao = reinsercaoNsga2(populacao, quantidadePopulacao);
			}
			if("spea2".equals(metodo)) {
				spea2(populacao, funcoes, destinos);
				
				Collections.sort(populacao);
				for(int j = populacao.size(); j > quantidadePopulacao; j--) {
					populacao.remove(j-1);
				}
			}
		}
		return populacao;
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
	
	public static int delay(Arvore arvore, int[] destinos) {
		int soma = 0;
		for(int i = 0; i < destinos.length; i++) {
			soma = arvore.getRaiz().calculaDelay(destinos[i]);
		}
		return soma;
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
		List<Arvore> floresta = quebrarArvoreCrossover(pai1, pai2, destinos, origem);
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
			
			/*System.out.println("Adjacencia 1: ");
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
			System.out.println();*/
			
			verificarRepeticao(adjacencia1, adjacencia2);
			
			/*System.out.println("Adjacencia 1: ");
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
			System.out.println();*/
			
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
				/*System.out.print("Menor Caminho: ");
				for(Vertice v : menorCaminho) {
					System.out.print(v.getDescricao()+", ");
				}
				System.out.println(); */
				List<Adjacencia> fusao = juntarListasAdjacencias(adjacencia1, adjacencia2, menorCaminho, origem, grafoAux);
				if(arvore1 < arvore2) {
					floresta.remove(arvore2);
					floresta.remove(arvore1);
				} else {
					floresta.remove(arvore1);
					floresta.remove(arvore2);
				}
				floresta.add(adjacenteParaArvore(fusao, origem));
			} else {
				if(arvore1 < arvore2) {
					floresta.remove(arvore2);
					floresta.remove(arvore1);
				} else {
					floresta.remove(arvore1);
					floresta.remove(arvore2);
				}
				floresta.add(adjacenteParaArvore(adjacencia1, origem));
			}
		}
		floresta.get(0).getRaiz().limpaArvore(destinos);
		return floresta.get(0);
	}
	
	public static Arvore mutacao(Arvore filho, int[] destinos, List<Enlace> enlaces, int delayMax, int origem) {
		List<Arvore> floresta = quebraArvoreMutacao(filho, destinos, origem);
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
				if(calculaDelay(filho, destinos, delayMax) != 1.0) {
					menorCaminho= dijkstra.encontrarMenorCaminhoDijkstraDelay(grafoAux, aux1, aux2);
				} else {
					menorCaminho= dijkstra.encontrarMenorCaminhoDijkstraCusto(grafoAux, aux1, aux2);
				}
				List<Adjacencia> fusao = juntarListasAdjacencias(adjacencia1, adjacencia2, menorCaminho, origem, grafoAux);
				if(arvore1 < arvore2) {
					floresta.remove(arvore2);
					floresta.remove(arvore1);
				} else {
					floresta.remove(arvore1);
					floresta.remove(arvore2);
				}
				floresta.add(adjacenteParaArvore(fusao, origem));
			} else {
				if(arvore1 < arvore2) {
					floresta.remove(arvore2);
					floresta.remove(arvore1);
				} else {
					floresta.remove(arvore1);
					floresta.remove(arvore2);
				}
				floresta.add(adjacenteParaArvore(adjacencia1, origem));
			}
		}
		floresta.get(0).getRaiz().limpaArvore(destinos);
		return floresta.get(0);
	}
	
	public static List<Arvore> quebraArvoreMutacao(Arvore arvore, int[] destinos, int origem) {
		List<Arvore> floresta = new ArrayList<>();
		List<Adjacencia> listaAdjacencia = montarListaAdjacencias(arvore.getRaiz(), new ArrayList<Adjacencia>(), new ArrayList<Integer>());
		
		Random rand = new Random();
		int corte1 = 1 + rand.nextInt(listaAdjacencia.size() - 1);
		int corte2 = 1 + rand.nextInt(listaAdjacencia.size() - 1);
		while(corte1 == corte2) {
			corte2 = 1 + rand.nextInt(listaAdjacencia.size() - 1);
		}
		
		/*listaAdjacencia = new ArrayList<Adjacencia>();
		Adjacencia mock1 = new Adjacencia();
		mock1.setValor(1);
		mock1.setAdjacencias(new ArrayList<Integer>());
		mock1.getAdjacencias().add(4);
		mock1.getAdjacencias().add(2);
		listaAdjacencia.add(mock1);
		
		Adjacencia mock2 = new Adjacencia();
		mock2.setValor(4);
		mock2.setAdjacencias(new ArrayList<Integer>());
		mock2.getAdjacencias().add(9);
		listaAdjacencia.add(mock2);
		
		Adjacencia mock3 = new Adjacencia();
		mock3.setValor(9);
		mock3.setAdjacencias(new ArrayList<Integer>());
		listaAdjacencia.add(mock3);
		
		Adjacencia mock4 = new Adjacencia();
		mock4.setValor(2);
		mock4.setAdjacencias(new ArrayList<Integer>());
		mock4.getAdjacencias().add(5);
		listaAdjacencia.add(mock4);
		
		Adjacencia mock5 = new Adjacencia();
		mock5.setValor(5);
		mock5.setAdjacencias(new ArrayList<Integer>());
		mock5.getAdjacencias().add(10);
		mock5.getAdjacencias().add(3);
		listaAdjacencia.add(mock5);
		
		Adjacencia mock6 = new Adjacencia();
		mock6.setValor(10);
		mock6.setAdjacencias(new ArrayList<Integer>());
		listaAdjacencia.add(mock6);
		
		Adjacencia mock7 = new Adjacencia();
		mock7.setValor(3);
		mock7.setAdjacencias(new ArrayList<Integer>());
		mock7.getAdjacencias().add(6);
		listaAdjacencia.add(mock7);
		
		Adjacencia mock8 = new Adjacencia();
		mock8.setValor(6);
		mock8.setAdjacencias(new ArrayList<Integer>());
		mock8.getAdjacencias().add(11);
		listaAdjacencia.add(mock8);
		
		Adjacencia mock9 = new Adjacencia();
		mock9.setValor(11);
		mock9.setAdjacencias(new ArrayList<Integer>());
		mock9.getAdjacencias().add(13);
		listaAdjacencia.add(mock9);
		
		Adjacencia mock10 = new Adjacencia();
		mock10.setValor(13);
		mock10.setAdjacencias(new ArrayList<Integer>());
		mock10.getAdjacencias().add(15);
		listaAdjacencia.add(mock10);
		
		Adjacencia mock11 = new Adjacencia();
		mock11.setValor(15);
		mock11.setAdjacencias(new ArrayList<Integer>());
		mock11.getAdjacencias().add(14);
		listaAdjacencia.add(mock11);
		
		Adjacencia mock12 = new Adjacencia();
		mock12.setValor(14);
		mock12.setAdjacencias(new ArrayList<Integer>());
		listaAdjacencia.add(mock12);
		
		for(Adjacencia ad : listaAdjacencia) {
			System.out.print(ad.getValor()+": ");
			for(Integer aux : ad.getAdjacencias()) {
				System.out.print(aux+", ");
			}
			System.out.println();
		}
		System.out.println();
		
		corte1 = 4;
		corte2 = 5;
		
		System.out.println(corte1);
		System.out.println(corte2);*/
		
		arvore = adjacenteParaArvore(listaAdjacencia, origem);
		
		int valorCorte2 = listaAdjacencia.get(corte2).getValor();
		
		if(listaAdjacencia.get(corte1).getAdjacencias().isEmpty()) {
			for(int i = 0; i < destinos.length; i++) {
				if(destinos[i] == listaAdjacencia.get(corte1).getValor()) {
					Arvore subArvore = new Arvore(destinos[i]);
					floresta.add(subArvore);
					break;
				}
			}
			int remover = -1;
			List<Integer> excluirFilhos = new ArrayList<>();
			for(int i = 0; i < listaAdjacencia.size(); i++) {
				if(listaAdjacencia.get(i).getValor() == listaAdjacencia.get(corte1).getValor()) {
					remover = i;
				}
				for(int j = 0; j < listaAdjacencia.get(i).getAdjacencias().size(); j++) {
					if(listaAdjacencia.get(i).getAdjacencias().get(j) == listaAdjacencia.get(corte1).getValor()) {
						excluirFilhos.add(j);
					}
				}
				for(int j = excluirFilhos.size(); j > 0; j--) {
					int aux = excluirFilhos.get(j-1);
					listaAdjacencia.get(i).getAdjacencias().remove(aux);
				}
				excluirFilhos = new ArrayList<Integer>();
			}
			listaAdjacencia.remove(remover);
			floresta.add(adjacenteParaArvore(listaAdjacencia, origem));
		} else {
			for(Integer aux : listaAdjacencia.get(corte1).getAdjacencias()) {
				No noAux = arvore.buscar(aux);
				List<No> listNoAux = new ArrayList<No>();
				for(No noAuxFilho : noAux.getFilhos()) {
					listNoAux.add(noAuxFilho);
				}
				Arvore arvoreAux = new Arvore(aux);
				arvoreAux.getRaiz().setFilhos(listNoAux);
				floresta.add(arvoreAux);
			}
			for(int i = 0; i < destinos.length; i++) {
				if(destinos[i] == listaAdjacencia.get(corte1).getValor()) {
					Arvore subArvore = new Arvore(destinos[i]);
					floresta.add(subArvore);
					break;
				}
			}
			Arvore arvoreCortada = adjacenteParaArvore(listaAdjacencia, origem);
			for(Adjacencia ad : listaAdjacencia) {
				for(Integer aux : ad.getAdjacencias()) {
					if(aux == listaAdjacencia.get(corte1).getValor()) {
						No no = arvoreCortada.buscar(ad.getValor());
						for(No noAux : no.getFilhos()) {
							if(noAux.getValor() == listaAdjacencia.get(corte1).getValor()) {
								no.getFilhos().remove(noAux);
								break;
							}
						}
					}
				}
			}
			floresta.add(arvoreCortada);
		}
		
		for(Arvore ar : floresta) {
			List<Adjacencia> adjacencia = montarListaAdjacencias(ar.getRaiz(), new ArrayList<Adjacencia>(), new ArrayList<Integer>());
			if(verificaValorLista(adjacencia, valorCorte2)) {
				int posicaoCorte = -1;
				for(int i = 0; i < adjacencia.size(); i++) {
					if(adjacencia.get(i).getValor() == valorCorte2) {
						posicaoCorte = i;
						break;
					}
				}
				if(adjacencia.get(posicaoCorte).getAdjacencias().isEmpty()) {
					for(int i = 0; i < destinos.length; i++) {
						if(destinos[i] == adjacencia.get(posicaoCorte).getValor()) {
							Arvore subArvore = new Arvore(destinos[i]);
							floresta.add(subArvore);
							break;
						}
					}
					if(!ar.getRaiz().getFilhos().isEmpty()) {
						int remover = -1;
						List<Integer> excluirFilhos = new ArrayList<>();
						for(int i = 0; i < adjacencia.size(); i++) {
							if(adjacencia.get(i).getValor() == adjacencia.get(posicaoCorte).getValor()) {
								remover = i;
							}
							for(int j = 0; j < adjacencia.get(i).getAdjacencias().size(); j++) {
								if(adjacencia.get(i).getAdjacencias().get(j) == adjacencia.get(posicaoCorte).getValor()) {
									excluirFilhos.add(j);
								}
							}
							for(int j = excluirFilhos.size(); j > 0; j--) {
								int aux = excluirFilhos.get(j-1);
								adjacencia.get(i).getAdjacencias().remove(aux);
							}
							excluirFilhos = new ArrayList<Integer>();
						}
						adjacencia.remove(remover);
						floresta.add(adjacenteParaArvore(adjacencia, origem));
					}
				} else {
					for(Integer aux : adjacencia.get(posicaoCorte).getAdjacencias()) {
						No noAux = arvore.buscar(aux);
						List<No> listNoAux = new ArrayList<No>();
						for(No noAuxFilho : noAux.getFilhos()) {
							listNoAux.add(noAuxFilho);
						}
						Arvore arvoreAux = new Arvore(aux);
						arvoreAux.getRaiz().setFilhos(listNoAux);
						floresta.add(arvoreAux);
					}
					for(int i = 0; i < destinos.length; i++) {
						if(destinos[i] == adjacencia.get(posicaoCorte).getValor()) {
							Arvore subArvore = new Arvore(destinos[i]);
							floresta.add(subArvore);
							break;
						}
					}
					if(valorCorte2 != ar.getRaiz().getValor()) {
						Arvore arvoreCortada = adjacenteParaArvore(adjacencia, origem);
						for(Adjacencia ad : adjacencia) {
							for(Integer aux : ad.getAdjacencias()) {
								if(aux == adjacencia.get(posicaoCorte).getValor()) {
									No no = arvoreCortada.buscar(ad.getValor());
									for(No noAux : no.getFilhos()) {
										if(noAux.getValor() == adjacencia.get(posicaoCorte).getValor()) {
											no.getFilhos().remove(noAux);
											break;
										}
									}
								}
							}
						}
						floresta.add(arvoreCortada);
					}
				}
				floresta.remove(ar);
				break;
			}
		}
		return floresta;
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
						if(!verificaFilhosIguais(lista1, aux)) {
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
					int aux = apagados.get(i-1);
					ad.getAdjacencias().remove(aux);
				}
			}
			listaRepetidos.remove(0);
		}
		if(!lista2.isEmpty()) {
			List<Integer> excluir = new ArrayList<>();
			for(int i = 0; i < lista2.get(0).getAdjacencias().size(); i++) {
				for(Adjacencia ad1 : lista1) {
					if(ad1.getValor() == lista2.get(0).getAdjacencias().get(i)) {
						excluir.add(i);
					}
				}
			}
			for(int i = excluir.size(); i > 0; i--) {
				int aux = excluir.get(i-1);
				lista2.get(0).getAdjacencias().remove(aux);
			}
		}
	}
	
	public static boolean verificaFilhosIguais(List<Adjacencia> lista1, int valor) {
		for(Adjacencia ad1 : lista1) {
			for(Integer aux1 : ad1.getAdjacencias()) {
				if(aux1 == valor) {
					return true;
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
							aux.setAdjacencias(new ArrayList<Integer>());
							aux.setValor(Integer.valueOf(menorCaminho.get(i+2).getDescricao()));
							listaAdjacencia.add(aux);
						} else if("aux2".equals(menorCaminho.get(i+1).getDescricao())) {
							boolean encontrouNaoRaix = false;
							//Se o no do menor caminho NÃO for raiz
							for(Adjacencia ad2: adjacencia2) {
								for(int k = 0; k < ad2.getAdjacencias().size(); k++) {
									if(menorCaminho.get(i+2).getDescricao().equals(String.valueOf(ad2.getAdjacencias().get(k)))) {
										encontrouNaoRaix = true;
										ad.getAdjacencias().add(ad2.getAdjacencias().get(k));
										aux = new Adjacencia();
										aux.setAdjacencias(new ArrayList<Integer>());
										for(Adjacencia ad3 : adjacencia2) {
											if(ad3.getValor() == ad2.getAdjacencias().get(k)) {
												aux.setAdjacencias(ad3.getAdjacencias());
											}
										}
										aux.setValor(ad2.getAdjacencias().get(k));
										listaAdjacencia.add(aux);
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getAdjacencias().get(k)) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
									}
								}
							}
							//Se o no do menor caminho for raiz
							if(!encontrouNaoRaix) {
								for(Adjacencia ad2: adjacencia2) {
									if(menorCaminho.get(i+2).getDescricao().equals(String.valueOf(ad2.getValor()))) {
										ad.getAdjacencias().add(ad2.getValor());
										aux = new Adjacencia();
										aux.setAdjacencias(ad2.getAdjacencias());
										aux.setValor(ad2.getValor());
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
									if(!verificaValorPai(listaAdjacencia, ad2.getValor()) && verificaValorFilho(listaAdjacencia, ad2.getValor()) && nosNaoVerificados.contains(ad2.getValor())) {
										aux = new Adjacencia();
										aux.setAdjacencias(ad2.getAdjacencias());
										aux.setValor(ad2.getValor());
										listaAdjacencia.add(aux);
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getValor()) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
									} else if(!verificaValorPai(listaAdjacencia, ad2.getValor()) && !verificaValorFilho(listaAdjacencia, ad2.getValor()) && nosNaoVerificados.contains(ad2.getValor())) {
										for(Integer aux2 : ad2.getAdjacencias()) {
											for(Adjacencia ad3 : listaAdjacencia) {
												if(aux2 == ad3.getValor()) {
													ad3.getAdjacencias().add(ad2.getValor());
													aux = new Adjacencia();
													aux.setAdjacencias(new ArrayList<Integer>());
													for(Integer aux3 : ad2.getAdjacencias()) {
														if(aux3 != aux2) {
															aux.getAdjacencias().add(aux3);
														}
													}
													aux.setValor(ad2.getValor());
													listaAdjacencia.add(aux);
													for(int j = 0; j < nosNaoVerificados.size(); j++) {
														if(nosNaoVerificados.get(j) == ad2.getValor()) {
															nosNaoVerificados.remove(j);
															break;
														}
													}
													break;
												}
											}
										}
									}
								}
							}
						} else if("aux1".equals(menorCaminho.get(i+1).getDescricao()) && ("aux2".equals(menorCaminho.get(i+2).getDescricao()))) {
							boolean encontrouNaoRaix = false;
							//Se o no do menor caminho NÃO for raiz
							for(Adjacencia ad2: adjacencia2) {
								for(int k = 0; k < ad2.getAdjacencias().size(); k++) {
									if(menorCaminho.get(i+3).getDescricao().equals(String.valueOf(ad2.getAdjacencias().get(k)))) {
										encontrouNaoRaix = true;
										ad.getAdjacencias().add(ad2.getAdjacencias().get(k));
										aux = new Adjacencia();
										aux.setAdjacencias(new ArrayList<Integer>());
										for(Adjacencia ad3 : adjacencia2) {
											if(ad3.getValor() == ad2.getAdjacencias().get(k)) {
												aux.setAdjacencias(ad3.getAdjacencias());
											}
										}
										aux.setValor(ad2.getAdjacencias().get(k));
										listaAdjacencia.add(aux);
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getAdjacencias().get(k)) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
									}
								}
							}
							//Se o no do menor caminho for raiz
							if(!encontrouNaoRaix) {
								for(Adjacencia ad2: adjacencia2) {
									if(menorCaminho.get(i+3).getDescricao().equals(String.valueOf(ad2.getValor()))) {
										ad.getAdjacencias().add(ad2.getValor());
										aux = new Adjacencia();
										aux.setAdjacencias(ad2.getAdjacencias());
										aux.setValor(ad2.getValor());
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
									if(!verificaValorPai(listaAdjacencia, ad2.getValor()) && verificaValorFilho(listaAdjacencia, ad2.getValor()) && nosNaoVerificados.contains(ad2.getValor())) {
										aux = new Adjacencia();
										aux.setAdjacencias(ad2.getAdjacencias());
										aux.setValor(ad2.getValor());
										listaAdjacencia.add(aux);
										for(int j = 0; j < nosNaoVerificados.size(); j++) {
											if(nosNaoVerificados.get(j) == ad2.getValor()) {
												nosNaoVerificados.remove(j);
												break;
											}
										}
									} else if(!verificaValorPai(listaAdjacencia, ad2.getValor()) && !verificaValorFilho(listaAdjacencia, ad2.getValor()) && nosNaoVerificados.contains(ad2.getValor())) {
										for(Integer aux2 : ad2.getAdjacencias()) {
											for(Adjacencia ad3 : listaAdjacencia) {
												if(aux2 == ad3.getValor()) {
													ad3.getAdjacencias().add(ad2.getValor());
													aux = new Adjacencia();
													aux.setAdjacencias(new ArrayList<Integer>());
													for(Integer aux3 : ad2.getAdjacencias()) {
														if(aux3 != aux2) {
															aux.getAdjacencias().add(aux3);
														}
													}
													aux.setValor(ad2.getValor());
													listaAdjacencia.add(aux);
													for(int j = 0; j < nosNaoVerificados.size(); j++) {
														if(nosNaoVerificados.get(j) == ad2.getValor()) {
															nosNaoVerificados.remove(j);
															break;
														}
													}
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
							aux.setAdjacencias(new ArrayList<Integer>());
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
		verificaRaizOrigem(listaAdjacencia, origem);
		/*System.out.println("Adjacencia merge: ");
		for(Adjacencia ad : listaAdjacencia) {
			System.out.print(ad.getValor()+": ");
			for(Integer aux : ad.getAdjacencias()) {
				System.out.print(aux+", ");
			}
			System.out.println();
		}
		System.out.println();*/
		return listaAdjacencia;
	}
	
	public static boolean verificaValorLista(List<Adjacencia> lista, int valor) {
		for(Adjacencia ad : lista) {
			if(ad.getValor() == valor) {
				return true;
			} else {
				for(Integer aux : ad.getAdjacencias()) {
					if(aux == valor) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean verificaValorPai(List<Adjacencia> lista, int valor) {
		for(Adjacencia ad : lista) {
			if(ad.getValor() == valor) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean verificaValorFilho(List<Adjacencia> lista, int valor) {
		for(Adjacencia ad : lista) {
			for(Integer aux : ad.getAdjacencias()) {
				if(aux == valor) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void verificaRaizOrigem(List<Adjacencia> lista, int origem) {
		if(origem != lista.get(0).getValor()) {
			boolean trocado = false;
			for(Adjacencia ad : lista) {
				if(ad.getValor() == origem) {
					for(int i = 0; i < lista.size(); i++) {
						for(int j = 0; j < lista.get(i).getAdjacencias().size(); j++) {
							if(lista.get(i).getAdjacencias().get(j) == origem) {
								lista.get(i).getAdjacencias().remove(j);
								ad.getAdjacencias().add(lista.get(i).getValor());
								Adjacencia aux = lista.get(i);
								lista.remove(i);
								lista.add(aux);
								trocado = true;
							}
						}
					}
					if(trocado) {
						break;
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
	
	public static List<Arvore> quebrarArvoreCrossover(Arvore pai1, Arvore pai2, int[] destinos, int origem) {
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
						floresta.add(adjacenteParaArvore(listaAdjacencia, origem));
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
	
	public static Arvore adjacenteParaArvore(List<Adjacencia> adjacentes, int origem) {
		Arvore arvore = new Arvore(adjacentes.get(0).getValor());
		if(verificaValorLista(adjacentes, origem) && adjacentes.get(0).getValor() != origem) {
			for(int i = 0; i < adjacentes.size(); i++) {
				if(adjacentes.get(i).getValor() == origem) {
					arvore = new Arvore(adjacentes.get(i).getValor());
					break;
				}
			}
		}
		List<Integer> faltaAdicionar = new ArrayList<Integer>();
		for(Adjacencia ad : adjacentes) {
			faltaAdicionar.add(ad.getValor());
		}
		while(!faltaAdicionar.isEmpty()) {
			No no = arvore.buscar(faltaAdicionar.get(0));
			if(no == null) {
				if(faltaAdicionar.size() == 1) {
					for(Adjacencia ad : adjacentes) {
						if(ad.getValor() == faltaAdicionar.get(0)) {
							no = arvore.buscar(ad.getAdjacencias().get(0));
							no.adicionaFilho(ad.getValor(), -1, -1);
							faltaAdicionar.remove(0);
							break;
						}
					}
				} else {
					int aux = faltaAdicionar.get(0);
					faltaAdicionar.remove(0);
					faltaAdicionar.add(aux);
				}
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
	
	public static void nsga2(List<Arvore> populacao, int[] destinos, boolean[] funcoes) {
		for(Arvore a : populacao) {
			a.setCusto(calculaCusto(a));
			a.setDelay(delay(a, destinos));
		}
		int quantidadeDeObjetivos = 0;
		for(boolean funcao : funcoes) {
			if(funcao) {
				quantidadeDeObjetivos++;
			}
		}
		//Função 1 sempre esta presente
		for(Arvore arvore : populacao) {
			arvore.setFuncao1(funcao1(arvore.getCusto()));
		}
		
		for(Arvore arvore : populacao) {
			if(quantidadeDeObjetivos == 2) {
				for(int i = 1; i < funcoes.length; i++) {
					if(funcoes[i] && i == 1) {
						arvore.setFuncao2(funcao2(arvore.getDelay()));
						break;
					} else if(funcoes[i] && i == 2) {
						arvore.setFuncao2(funcao3(arvore, destinos));
						break;
					} else if(funcoes[i] && i == 3) {
						arvore.setFuncao2(funcao4(arvore, destinos));
						break;
					}
				}
			} else if(quantidadeDeObjetivos == 3) {
				boolean funcao3 = false;
				for(int i = 1; i < funcoes.length; i++) {
					if(funcoes[i] && i == 1) {
						arvore.setFuncao2(funcao2(arvore.getDelay()));
						funcao3 = true;
					}
					if(!funcao3 && funcoes[i] && i == 2) {
						arvore.setFuncao2(funcao3(arvore, destinos));
						funcao3 = true;
					}
					if(funcao3 && funcoes[i] && i == 2) {
						arvore.setFuncao3(funcao3(arvore, destinos));
						break;
					}
					if(funcoes[i] && i == 3) {
						arvore.setFuncao3(funcao4(arvore, destinos));
						break;
					}
				}
			} else {
				System.out.println("Deu ruim");
			}
		}
		//Separa a população em fronteiras
		List<Fronteira> fronteiras = encontraFronteiras(populacao, quantidadeDeObjetivos, funcoes);
		
		//mostraFronteiras(fronteiras);
		
		for(Fronteira fronteira : fronteiras) {
			Arvore melhorFuncao1 = null;
			Arvore melhorFuncao2 = null;
			Arvore melhorFuncao3 = null;
			for(Arvore arvore : fronteira.getIndividuos()) {
				if(melhorFuncao1 == null || arvore.getFuncao1() > melhorFuncao1.getFuncao1()) {
					melhorFuncao1 = arvore;
				}
				if(melhorFuncao2 == null || (funcoes[1] && arvore.getFuncao2() > melhorFuncao2.getFuncao2()) || (!funcoes[1] && arvore.getFuncao2() < melhorFuncao2.getFuncao2())) {
					melhorFuncao2 = arvore;
				}
				if(quantidadeDeObjetivos == 3 && (melhorFuncao3 == null || arvore.getFuncao3() < melhorFuncao3.getFuncao3())) {
					melhorFuncao3 = arvore;
				}
			}
			melhorFuncao1.setCrowdingDistance(10000.0);
			melhorFuncao2.setCrowdingDistance(10000.0);
			if(quantidadeDeObjetivos == 3) {
				melhorFuncao3.setCrowdingDistance(10000.0);
			}
			
			Collections.sort(fronteira.getIndividuos(), new Comparator<Arvore>(){
		        public int compare(Arvore first, Arvore second){
		        	if (first.getFuncao1() < second.getFuncao1()) { 
		      		  return -1; 
		      		  } if (first.getFuncao1() > second.getFuncao1()) { 
		      		  return 1; 
		      		  } 
		      		  return 0; 
		      		 }
		        });

			for(int i = 0; i < fronteira.getIndividuos().size(); i++) {
				if(fronteira.getIndividuos().get(i).getCrowdingDistance() == 0.0) {
					if(i == 0) {
						fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i+1).getFuncao1());
					} else if(i == fronteira.getIndividuos().size() - 1) {
						fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i-1).getFuncao1());
					} else {
						fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i+1).getFuncao1() - fronteira.getIndividuos().get(i-1).getFuncao1());
					}
				}
			}
			
			Collections.sort(fronteira.getIndividuos(), new Comparator<Arvore>(){
		        public int compare(Arvore first, Arvore second){
		        	if (first.getFuncao2() < second.getFuncao2()) { 
		      		  return -1; 
		      		  } if (first.getFuncao2() > second.getFuncao2()) { 
		      		  return 1; 
		      		  } 
		      		  return 0; 
		      		 }
		        });
			
			for(int i = 0; i < fronteira.getIndividuos().size(); i++) {
				if(fronteira.getIndividuos().get(i).getCrowdingDistance() == 0.0) {
					if(i == 0) {
						fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i).getCrowdingDistance() + fronteira.getIndividuos().get(i+1).getFuncao2());
					} else if(i == fronteira.getIndividuos().size() - 1) {
						fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i).getCrowdingDistance() + fronteira.getIndividuos().get(i-1).getFuncao2());
					} else {
						fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i).getCrowdingDistance() + fronteira.getIndividuos().get(i+1).getFuncao2() - fronteira.getIndividuos().get(i-1).getFuncao2());
					}
				}
			}
			
			if(quantidadeDeObjetivos == 3) {
				Collections.sort(fronteira.getIndividuos(), new Comparator<Arvore>(){
			        public int compare(Arvore first, Arvore second){
			        	if (first.getFuncao3() < second.getFuncao3()) { 
			      		  return -1; 
			      		  } if (first.getFuncao3() > second.getFuncao3()) { 
			      		  return 1; 
			      		  } 
			      		  return 0; 
			      		 }
			        });
				
				for(int i = 0; i < fronteira.getIndividuos().size(); i++) {
					if(fronteira.getIndividuos().get(i).getCrowdingDistance() == 0.0) {
						if(i == 0) {
							fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i).getCrowdingDistance() + fronteira.getIndividuos().get(i+1).getFuncao3());
						} else if(i == fronteira.getIndividuos().size() - 1) {
							fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i).getCrowdingDistance() + fronteira.getIndividuos().get(i-1).getFuncao3());
						} else {
							fronteira.getIndividuos().get(i).setCrowdingDistance(fronteira.getIndividuos().get(i).getCrowdingDistance() + fronteira.getIndividuos().get(i+1).getFuncao3() - fronteira.getIndividuos().get(i-1).getFuncao3());
						}
					}
				}
			}
		}
		for(int i = 0; i < fronteiras.size(); i++) {
			for(Arvore arvore : fronteiras.get(i).getIndividuos()) {
				arvore.setFronteira(i+1);
			}
		}
	}
	
	public static double funcao1(int custo) {
		return 1.0/custo;
	}
	
	public static double funcao2(int delay) {
		return 1.0/delay;
	}
	
	public static double funcao3(Arvore arvore, int[] destinos) {
		int soma = 0;
		for(int i = 0; i < destinos.length; i++) {
			soma = arvore.getRaiz().calculaDelay(destinos[i]);
		}
		return  ((double) soma)/ ((double) destinos.length);
	}
	
	public static double funcao4(Arvore arvore, int[] destinos) {
		int maior = 0;
		for(int i = 0; i < destinos.length; i++) {
			int aux = arvore.getRaiz().calculaDelay(destinos[i]);
			if(aux > maior) {
				maior = aux;
			}
		}
		return (double) maior;
	}
	
	public static List<Fronteira> encontraFronteiras(List<Arvore> populacao, int quantidadeObjetivos, boolean[] funcoes) {
		List<Fronteira> fronteiras = new ArrayList<>();
		List<Arvore> individuosSemFronteira = new ArrayList<>();
		List<Integer> remover = new ArrayList<>();
		
		int fronteira = 0;
		
		for(Arvore arvore : populacao) {
			individuosSemFronteira.add(arvore);
		}
		
		while(!individuosSemFronteira.isEmpty()) {
			fronteira++;
			for(int i = 0; i < individuosSemFronteira.size(); i++) {
				if(quantidadeObjetivos == 2) {
					boolean dominado = false;
					for(Arvore arvore : individuosSemFronteira) {
						if((individuosSemFronteira.get(i).getFuncao1() <= arvore.getFuncao1() && (funcoes[1] && individuosSemFronteira.get(i).getFuncao2() < arvore.getFuncao2() || !funcoes[1] && individuosSemFronteira.get(i).getFuncao2() > arvore.getFuncao2()))
								|| (individuosSemFronteira.get(i).getFuncao1() < arvore.getFuncao1() && (funcoes[1] && individuosSemFronteira.get(i).getFuncao2() <= arvore.getFuncao2() || !funcoes[1] && individuosSemFronteira.get(i).getFuncao2() >= arvore.getFuncao2()))) {
							dominado = true;
							break;
						}
					}
					if(!dominado) {
						remover.add(i);
					}
				} else if(quantidadeObjetivos == 3) {
					boolean dominado = false;
					for(Arvore arvore : individuosSemFronteira) {
						if((individuosSemFronteira.get(i).getFuncao1() <= arvore.getFuncao1() && (funcoes[1] && individuosSemFronteira.get(i).getFuncao2() <= arvore.getFuncao2() || !funcoes[1] && individuosSemFronteira.get(i).getFuncao2() >= arvore.getFuncao2()) && individuosSemFronteira.get(i).getFuncao3() > arvore.getFuncao3())
								|| (individuosSemFronteira.get(i).getFuncao1() < arvore.getFuncao1() && (funcoes[1] && individuosSemFronteira.get(i).getFuncao2() <= arvore.getFuncao2() || !funcoes[1] && individuosSemFronteira.get(i).getFuncao2() >= arvore.getFuncao2()) && individuosSemFronteira.get(i).getFuncao3() >= arvore.getFuncao3()) ||
								(individuosSemFronteira.get(i).getFuncao1() <= arvore.getFuncao1() && (funcoes[1] && individuosSemFronteira.get(i).getFuncao2() < arvore.getFuncao2() || !funcoes[1] && individuosSemFronteira.get(i).getFuncao2() > arvore.getFuncao2()) && individuosSemFronteira.get(i).getFuncao3() >= arvore.getFuncao3())) {
							dominado = true;
							break;
						}
					}
					if(!dominado) {
						remover.add(i);
					}
				}
			}
			Fronteira auxFronteira = new Fronteira(fronteira);
			for(Integer remove : remover) {
				auxFronteira.getIndividuos().add(individuosSemFronteira.get(remove));
			}
			fronteiras.add(auxFronteira);
			for(int i = remover.size(); i > 0; i--) {
				int aux = remover.get(i-1);
				individuosSemFronteira.remove(aux);
			}
			remover = new ArrayList<Integer>();
		}
		
		return fronteiras;
	}
	
	public static void mostraFronteiras(List<Fronteira> fronteiras) {
		for(Fronteira fronteira : fronteiras) {
			System.out.println("Fronteira "+ fronteira.getFronteira() +" :");
			for(Arvore arvore : fronteira.getIndividuos()) {
				System.out.println("F1: "+arvore.getFuncao1()+"\t F2: "+arvore.getFuncao2()+"\t F3: "+ arvore.getFuncao3());
			}
		}
	}
	
	public static int torneio2nsga2(List<Arvore> populacao) {
		Random rand = new Random();
		
		int sorteio1 = rand.nextInt(populacao.size());
		int sorteio2 = rand.nextInt(populacao.size());
		
		while(sorteio1 == sorteio2) {
			sorteio2 = rand.nextInt(populacao.size());
		}
		
		if(populacao.get(sorteio1).getFronteira() < populacao.get(sorteio2).getFronteira()) {
			return sorteio1;
		} else if(populacao.get(sorteio1).getFronteira() > populacao.get(sorteio2).getFronteira()) {
			return sorteio2;
		} else if(populacao.get(sorteio1).getFronteira() == populacao.get(sorteio2).getFronteira()) {
			if(populacao.get(sorteio1).getCrowdingDistance() < populacao.get(sorteio2).getCrowdingDistance()) {
				return sorteio1;
			} else {
				return sorteio2;
			}
		}
		return -1;
	}

	public static List<Arvore> reinsercaoNsga2(List<Arvore> populacao, int quantidadePopulacao) {
		List<Arvore> novaPopulacao = new ArrayList<Arvore>();
		int fronteiraAtual = 0;
		Fronteira fronteira = new Fronteira(0);
		List<Fronteira> fronteiras = new ArrayList<>();
		
		for(int i = 0; i < populacao.size(); i++) {
			if(fronteiraAtual != populacao.get(i).getFronteira()) {
				fronteiraAtual = populacao.get(i).getFronteira();
				fronteira = new Fronteira(fronteiraAtual);
			}
			fronteira.getIndividuos().add(populacao.get(i));
			if(i+1 == populacao.size() || populacao.get(i+1).getFronteira() != fronteiraAtual) {
				fronteiras.add(fronteira);
			}
		}
		
		for(int i = 0; i < fronteiras.size(); i++) {
			if(fronteiras.get(i).getIndividuos().size() <= quantidadePopulacao - novaPopulacao.size()) {
				for(int j = 0; j < fronteiras.get(i).getIndividuos().size(); j++) {
					novaPopulacao.add(fronteiras.get(i).getIndividuos().get(j));
				}
			} else {
				Collections.sort(populacao, new Comparator<Arvore>(){
			        public int compare(Arvore first, Arvore second){
			        	if (first.getCrowdingDistance() < second.getCrowdingDistance()) { 
			      		  return -1; 
			      		  } if (first.getCrowdingDistance() > second.getCrowdingDistance()) { 
			      		  return 1; 
			      		  } 
			      		  return 0; 
			      		 }
			        });
				for(int j = novaPopulacao.size(), k = 0; j < quantidadePopulacao; j++, k++) {
					novaPopulacao.add(fronteiras.get(i).getIndividuos().get(k));
				}
			}
		}
		return novaPopulacao;
	}
	
	public static void spea2(List<Arvore> populacao, boolean[] funcoes, int[] destinos) {
		
		for(Arvore a : populacao) {
			a.setCusto(calculaCusto(a));
			a.setDelay(delay(a, destinos));
		}
		
		int quantidadeDeObjetivos = 0;
		for(boolean funcao : funcoes) {
			if(funcao) {
				quantidadeDeObjetivos++;
			}
		}
		
		//Função 1 sempre esta presente
		for(Arvore arvore : populacao) {
			arvore.setFuncao1(funcao1(arvore.getCusto()));
		}
		
		for(Arvore arvore : populacao) {
			if(quantidadeDeObjetivos == 2) {
				for(int i = 1; i < funcoes.length; i++) {
					if(funcoes[i] && i == 1) {
						arvore.setFuncao2(funcao2(arvore.getDelay()));
						break;
					} else if(funcoes[i] && i == 2) {
						arvore.setFuncao2(funcao3(arvore, destinos));
						break;
					} else if(funcoes[i] && i == 3) {
						arvore.setFuncao2(funcao4(arvore, destinos));
						break;
					}
				}
			} else if(quantidadeDeObjetivos == 3) {
				boolean funcao3 = false;
				for(int i = 1; i < funcoes.length; i++) {
					if(funcoes[i] && i == 1) {
						arvore.setFuncao2(funcao2(arvore.getDelay()));
						funcao3 = true;
					}
					if(!funcao3 && funcoes[i] && i == 2) {
						arvore.setFuncao2(funcao3(arvore, destinos));
						funcao3 = true;
					}
					if(!funcao3 && funcoes[i] && i == 2) {
						arvore.setFuncao3(funcao3(arvore, destinos));
						break;
					}
					if(funcoes[i] && i == 3) {
						arvore.setFuncao3(funcao4(arvore, destinos));
						break;
					}
				}
			} else {
				System.out.println("Deu ruim");
			}
		}
		 
		 quantidadeDeIndividuosDominantes(populacao, quantidadeDeObjetivos, funcoes);
		 
		 calculaStrengths(populacao, funcoes, quantidadeDeObjetivos);
		 
		 calculaDensidade(populacao, quantidadeDeObjetivos);
		 
		 for(Arvore ar : populacao) {
			 ar.setAptidao(ar.getStrengths() + ar.getDensidade());
		 }
	}
	
	public static void quantidadeDeIndividuosDominantes(List<Arvore> populacao, int quantidadeObjetivos, boolean[] funcoes) {
		for(Arvore ar : populacao) {
			int cont = 0;
			for(Arvore ar2 : populacao) {
				if(quantidadeObjetivos == 2 && ar.getFuncao1() > ar2.getFuncao1() && (funcoes[1] && ar.getFuncao2() > ar2.getFuncao2() || !funcoes[1] && ar.getFuncao2() < ar2.getFuncao2())) {
					cont++;
				}
				if(quantidadeObjetivos == 3 && ar.getFuncao1() > ar2.getFuncao1() && (funcoes[1] && ar.getFuncao2() > ar2.getFuncao2() || !funcoes[1] && ar.getFuncao2() < ar2.getFuncao2()) && ar.getFuncao3() < ar2.getFuncao3()) {
					cont++;
				}
			}
			ar.setNumeroDominados(cont);
		}
	}
	
	private static void calculaStrengths(List<Arvore> populacao, boolean[] funcoes, int quantidadeObjetivos) {
		for(Arvore ar : populacao) {
			if(ar.getFronteira() != 1) {
				for(Arvore ar2 : populacao) {
					if(quantidadeObjetivos == 2 && ar.getFuncao1() < ar2.getFuncao1() && (funcoes[1] && ar.getFuncao2() < ar2.getFuncao2() || !funcoes[1] && ar.getFuncao2() > ar2.getFuncao2())) {
						ar.setStrengths(ar.getStrengths() + ar2.getNumeroDominados());
					}
					if(quantidadeObjetivos == 3 && ar.getFuncao1() < ar2.getFuncao1() && (funcoes[1] && ar.getFuncao2() < ar2.getFuncao2() || !funcoes[1] && ar.getFuncao2() > ar2.getFuncao2()) && ar.getFuncao3() > ar2.getFuncao3()) {
						ar.setStrengths(ar.getStrengths() + ar2.getNumeroDominados());
					}
				}
			}
		}
	}
	
	private static void calculaDensidade(List<Arvore> populacao, int quantidadeObjetivos) {
		for(Arvore ar: populacao) {
			ar.setDensidade(1.0/(vizinhoMaisProximo(populacao, ar, quantidadeObjetivos) + 1.0));
		}
	}
	
	private static double vizinhoMaisProximo(List<Arvore> populacao, Arvore arvore, int quantidadeObjetivos) {
		double menorDistancia = -1;
		for(Arvore ar : populacao) {
			double distancia = distanciaEntreDoisPontos(arvore, ar, quantidadeObjetivos);
			if(menorDistancia == -1 || menorDistancia > distancia || menorDistancia == 0) {
				menorDistancia = distancia;
			}
		}
		if(menorDistancia == -1) {
			menorDistancia = 0;
		}
		return menorDistancia;
	}
	
	private static double distanciaEntreDoisPontos(Arvore arvore1, Arvore arvore2, int quantidadeObjetivos) {
		if(quantidadeObjetivos == 2) {
			return Math.sqrt(Math.pow(arvore2.getFuncao1() - arvore1.getFuncao1(), 2) + Math.pow(arvore2.getFuncao2() - arvore1.getFuncao2(), 2));
		} else {
			return Math.sqrt(Math.pow(arvore2.getFuncao1() - arvore1.getFuncao1(), 2) + Math.pow(arvore2.getFuncao2() - arvore1.getFuncao2(), 2) + Math.pow(arvore2.getFuncao3() - arvore1.getFuncao3(), 2));
		}
	}
	
	private static double errorRate(List<Arvore> populacao, String algoritmo) {
		int contador = 0;
		if("nsga2".equals(algoritmo)) {
			for(Arvore ar : populacao) {
				if(ar.getFronteira() == 1) {
					contador++;
				}
			}
			return (double)populacao.size()/(double)contador;
		}
		if("spea2".equals(algoritmo)) {
			for(Arvore ar: populacao) {
				if(ar.getStrengths() == 0) {
					contador++;
				}
			}
			return (double)populacao.size()/(double)contador;
		}
		return 0;
	}
	
	private static void mostraOtimoPareto(List<Arvore> populacao, String algoritmo) {
		int contador = 0;
		if("nsga2".equals(algoritmo)) {
			for(Arvore ar : populacao) {
				if(ar.getFronteira() == 1) {
					contador++;
					System.out.println(String.format(contador + " - Custo: " + ar.getCusto() + "\t delay: " + ar.getDelay() + "\t Funcao 1: %.5f\t Funcao 2: %.5f\t Funcao 3: %.5f", ar.getFuncao1(), ar.getFuncao2(), ar.getFuncao3()));
				}
			}
		}
		if("spea2".equals(algoritmo)) {
			for(Arvore ar : populacao) {
				if(ar.getStrengths() == 0) {
					contador++;
					System.out.println(String.format(contador + " - Custo: " + ar.getCusto() + "\t delay: " + ar.getDelay() + "\t Funcao 1: %.5f\t Funcao 2: %.5f\t Funcao 3: %.5f", ar.getFuncao1(), ar.getFuncao2(), ar.getFuncao3()));
				}
			}
		}
	}
	
	private static double paretoSubset(List<Arvore> populacao, String algoritmo) {
		return Math.abs((1.0 - errorRate(populacao, algoritmo))) * populacao.size();
	}
	
	private static int torneio3(List<Arvore> populacao) {
		Random rand = new Random();
		
		int sorteio1 = rand.nextInt(populacao.size());
		int sorteio2 = rand.nextInt(populacao.size());
		int sorteio3 = rand.nextInt(populacao.size());
		
		while(sorteio1 == sorteio2) {
			sorteio2 = rand.nextInt(populacao.size());
		}
		
		while(sorteio3 == sorteio1 || sorteio3 == sorteio2) {
			sorteio3 = rand.nextInt(populacao.size());
		}
		
		if(populacao.get(sorteio1).getAptidao() < populacao.get(sorteio2).getAptidao() && populacao.get(sorteio1).getAptidao() < populacao.get(sorteio3).getAptidao()) {
			return sorteio1;
		} else if(populacao.get(sorteio2).getAptidao() < populacao.get(sorteio1).getAptidao() && populacao.get(sorteio2).getAptidao() < populacao.get(sorteio3).getAptidao()) {
			return sorteio2;
		} else {
			return sorteio3;
		}
	}
	
	private static double spread(List<Arvore> populacao, boolean[] funcoes) {
		
		int quantidadeDeObjetivos = 0;
		double spread = 0;
		for(boolean funcao : funcoes) {
			if(funcao) {
				quantidadeDeObjetivos++;
			}
		}
		
		if(quantidadeDeObjetivos == 2) {
			int melhorFuncao1 = -1;
			int melhorFuncao2 = -1;
			for(int i = 0; i < populacao.size(); i++) {
				if(melhorFuncao1 == -1 || populacao.get(melhorFuncao1).getFuncao1() < populacao.get(i).getFuncao1()) {
					melhorFuncao1 = i;
				}
				if(melhorFuncao2 == -1 || (funcoes[1] && populacao.get(melhorFuncao2).getFuncao2() < populacao.get(i).getFuncao2()) || (!funcoes[1] && populacao.get(melhorFuncao2).getFuncao2() > populacao.get(i).getFuncao2())) {
					melhorFuncao2 = i;
				}
			}
			double distanciaEntreExtremos = distanciaEntreDoisPontos(populacao.get(melhorFuncao1), populacao.get(melhorFuncao2), quantidadeDeObjetivos);
			
			Fronteira fronteira = new Fronteira(0);
			
			if(populacao.get(0).getFronteira() == 0) { //spea2
				fronteira = procuraFronteiraSpea(populacao);
			} else { //nsga2
				List<Fronteira> fronteiras = encontraFronteiras(populacao, quantidadeDeObjetivos, funcoes);
				fronteira = fronteiras.get(0);
			}
		} else {
			int melhorFuncao1 = -1;
			int melhorFuncao2 = -1;
			int melhorFuncao3 = -1;
			for(int i = 0; i < populacao.size(); i++) {
				if(melhorFuncao1 == -1 || populacao.get(melhorFuncao1).getFuncao1() < populacao.get(i).getFuncao1()) {
					melhorFuncao1 = i;
				}
				if(melhorFuncao2 == -1 || (funcoes[1] && populacao.get(melhorFuncao2).getFuncao2() < populacao.get(i).getFuncao2()) || (!funcoes[1] && populacao.get(melhorFuncao2).getFuncao2() > populacao.get(i).getFuncao2())) {
					melhorFuncao2 = i;
				}
				if(melhorFuncao3 == -1 || populacao.get(melhorFuncao3).getFuncao3() > populacao.get(i).getFuncao3()) {
					melhorFuncao3 = i;
				}
			}
			double distanciaEntreExtremos = distanciaEntreDoisPontos(populacao.get(melhorFuncao1), populacao.get(melhorFuncao2), quantidadeDeObjetivos);
			distanciaEntreExtremos += distanciaEntreDoisPontos(populacao.get(melhorFuncao1), populacao.get(melhorFuncao3), quantidadeDeObjetivos);
			distanciaEntreExtremos += distanciaEntreDoisPontos(populacao.get(melhorFuncao2), populacao.get(melhorFuncao3), quantidadeDeObjetivos);
			
			Fronteira fronteira = new Fronteira(0);
			
			if(populacao.get(0).getFronteira() == 0) { //spea2
				fronteira = procuraFronteiraSpea(populacao);
			} else { //nsga2
				List<Fronteira> fronteiras = encontraFronteiras(populacao, quantidadeDeObjetivos, funcoes);
				fronteira = fronteiras.get(0);
			}
		}
		return 0;
	}
	
	public static Fronteira procuraFronteiraSpea(List<Arvore> populacao) {
		Fronteira fronteira = new Fronteira(1);
		for(Arvore ar : populacao) {
			if(ar.getStrengths() == 0) {
				fronteira.getIndividuos().add(ar);
			}
		}
		return fronteira;
	}
	
	/*public static double calculoDistanciaMedia(Fronteira fronteira) {
		
	}
	
	public static double calculoDistanciaVizinhos(Fronteira fronteira, Arvore arvore, int quantidadeDeObjetivos) {
		int menor = -1;
		int maior = -1;
		for(Arvore ar : fronteira.getIndividuos()) {
			if(ar.get)
		}
	}*/
}
