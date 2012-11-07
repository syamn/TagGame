/**
 * TagGame - Package: syam.taggame.exception
 * Created: 2012/11/07 19:31:28
 */
package syam.taggame.exception;

/**
 * TagGameException (TagGameException.java)
 * @author syam(syamn)
 */
public class TagGameException extends RuntimeException{
	private static final long serialVersionUID = -7756335851829554452L;

	public TagGameException(String message){
		super(message);
	}

	public TagGameException(Throwable cause){
		super(cause);
	}

	public TagGameException(String message, Throwable cause){
		super(message, cause);
	}
}
