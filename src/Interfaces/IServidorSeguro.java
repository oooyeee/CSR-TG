package interfaces;

public interface IServidorSeguro {
    public void useValidator(IValidator validator);
    public void useSecureCipherClass(Class<? extends ISecureCipher> secureCipherClass);
    public void useCommandRouter(ICommandRouter router); // Comandos
}
