package br.com.lucene.vetorial;

import java.io.File;

import org.apache.log4j.Logger;

import br.com.lucene.Util;

public class Experimento {
	private static Logger logger = Logger.getLogger(Experimento.class);

	private Preparador preparador;
	private Indexador indexador;
	private Buscador buscador;

	public Experimento(Preparador preparador, Indexador indexador, Buscador buscador) {
		this.preparador = preparador;
		this.indexador = indexador;
		this.buscador = buscador;
	}

	/**
	 * Compara 1 arquivo-consulta com todos da base, e assim por diante...
	 * Reindexando o perfil do autor desse arquivo antes da comparacao 
	 * A fim de encontrar o autor desse arquivo
	 */
	public void testar(File dirBase, File dirBasePreparada, String sufixoAceito) {
		// Informações do experimento
		int numExperimentos = 0;
		int numAcertos = 0;
		
		String autorVerdadeiro = "";
		String autorScap = "";
		String autorAnterior = "";

		for (File arquivoConsulta : Util.listarArquivosDiretorio(dirBase, "", sufixoAceito)) {
			autorVerdadeiro = Util.getNomeAutor(arquivoConsulta);
			autorScap = "";

			if (autorAnterior != "" && autorAnterior != autorVerdadeiro) {
				// Concatena todos os n-grams do autor anterior
				preparador.prepararDiretorio(dirBase, autorAnterior, sufixoAceito, null);
			}

			// Concatena os n-grams deste autor (sem o arquivoConsulta)
			preparador.prepararDiretorio(dirBase, autorVerdadeiro, sufixoAceito, arquivoConsulta);
			
			// Indexa o diretório
			indexador.indexarDiretorio(dirBasePreparada, sufixoAceito);

			// Faz a consulta/comparacao e sugere quem e o autor do arquivo
			autorScap = buscador.buscar(arquivoConsulta);
//			System.out.println("Autor: " + autorScap);
		}

	}

}
