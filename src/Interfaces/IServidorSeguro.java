package interfaces;

public interface IServidorSeguro {
    public void useValidator(IValidator validator);
    public void useSecureCipherClass(Class<? extends ISecureCipher> secureCipherClass);
}
