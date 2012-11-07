/**
 * TagGame - Package: syam.taggame.exception
 * Created: 2012/11/07 19:30:47
 */
package syam.taggame.exception;

/**
 * GameStateException (GameStateException.java)
 * @author syam(syamn)
 */
public class GameStateException extends TagGameException{
	private static final long serialVersionUID = -557041079228694288L;

	public GameStateException(String message){
		super(message);
	}

	public GameStateException(Throwable cause){
		super(cause);
	}

	public GameStateException(String message, Throwable cause){
		super(message, cause);
	}
}
