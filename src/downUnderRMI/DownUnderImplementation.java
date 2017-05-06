package downUnderRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class DownUnderImplementation extends UnicastRemoteObject implements DownUnderInterface {
	
	private static int MAX_PARTIDAS = 50;
	
	private int idCount = 10;
	
	private static final long serialVersionUID = 189L;
	
	private ArrayList<Partida> partidas = new ArrayList<>();
	
	public DownUnderImplementation() throws RemoteException {
	}

	@Override
	public int registraJogador(String nomeJogador) throws RemoteException {
		if (partidas.size() == MAX_PARTIDAS*2)
			return -2;
		
		Partida partidaExistente = buscaPartidaPorJogador(nomeJogador);
		
		if (partidaExistente != null)
			return -1;
		
		Jogador novo = new Jogador(nomeJogador, idCount);
		
		if(!alocaJogador(novo))
			return -2;

		return novo.getId();
	}

	@Override
	public int encerraPartida(int idJogador) throws RemoteException {
		int indexPartida = -1;
		
		for (int i=0;i<partidas.size();i++)
			if (partidas.get(i).verificaJogador(idJogador))
				indexPartida = i;
		
		if (indexPartida  < 0)
			return -1;
		
		// apaga referencia so quando os dois jogadores deixam partida
		if(partidas.get(indexPartida).removeJogador(idJogador))
			partidas.remove(indexPartida);
		
		return 0;
	}

	@Override
	public int temPartida(int idJogador) throws RemoteException {
		try {
			Partida partida = buscaPartidaPorJogador(idJogador);

			if(partida.statusPartida() == StatusPartida.AGUARDANDO)
				return 0;
			
			if (partida.getJogador1().getId() == idJogador)
				return 1;
			else
				return 2;

		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public int minhaVez(int idjogador) throws RemoteException {
		try {
			Partida partida = buscaPartidaPorJogador(idjogador);

			switch(partida.statusPartida()) {
				case AGUARDANDO :
					return -2;
				case ENCERRADA :
					if (partida.getVencedor() == 0) // empate
						return 4;
					if (partida.getVencedor() == idjogador) { // ganhou
						if (partida.tipoVitoria() == 0)
							return 2;
						return 5;
					}	
					else {
						if (partida.tipoVitoria() == 0) // perdeu
							return 3;
						return 6;
					}
				case INICIADA:
					if (partida.getJogadorAtual().getId() == idjogador)
						return 1;
					else
						return 0;
				default :
					return -1;
			}

		} catch (Exception e) {
			return 1;
		}
	}

	@Override
	public String obtemTabuleiro(int idJogador) throws RemoteException {
		try {
			Partida partida = buscaPartidaPorJogador(idJogador);

			return partida.getTabuleiro();
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public int soltaEsfera(int idJogador, int posicao) throws RemoteException {
		// �2�(partida�encerrada,�o�que�ocorrer�caso�o�jogador�demore�muito�para�enviar�a�sua
		// jogada�e�ocorra�o�time�out�de�60�segundos�para�envio�de�jogadas),�1�(tudo�certo),�0�(movimento
		// inv�lido,�por�exemplo,�em�um�orif�cio�que�j� �tem�8�esferas),��1�(erro:�n�mero�inv�lido�de
		// orif�cio),��2�(partida�n�o�iniciada:�ainda�n�o�h�dois�jogadores�registrados�na�partida),��3�(n�o��a
		// vez�do�jogador).
		Partida partida = buscaPartidaPorJogador(idJogador);

		if (partida.statusPartida() == StatusPartida.AGUARDANDO)
			return -2;

		if (partida.getJogadorAtual().getId() != idJogador)
			return -3;
		
		return partida.realizaJogada(idJogador, posicao);
	}

	@Override
	public String obtemOponente(int idJogador) throws RemoteException {
		try {
			Partida partida = buscaPartidaPorJogador(idJogador);

			return partida.getOponente(idJogador);
		} catch (Exception e) {
			return "";
		}
	}
	
	private Partida buscaPartidaPorJogador(int id) {
		for (Partida partida : partidas)
			if (partida.verificaJogador(id))
				return partida;
		
		return null;
	}
	
	private Partida buscaPartidaPorJogador(String nomeJogador) {
		for (Partida partida : partidas)
			if (partida.verificaJogador(nomeJogador))
				return partida;
		
		return null;
	}

	private boolean alocaJogador(Jogador jogador) {
		// TODO: ADICIONAR TIMER PARA ESPERA DO SEGUNDO JOGADOR
		Partida partidaDisponivel = partidas.get(partidas.size()-1);
		
		if(partidaDisponivel.statusPartida() == StatusPartida.INICIADA) {
			partidaDisponivel = new Partida(jogador);
			partidas.add(partidaDisponivel);
			return true;
		} else
			return partidaDisponivel.adicionaOponente(jogador);
	}

}

