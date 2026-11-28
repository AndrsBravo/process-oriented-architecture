package poa.shared.rules;

@FunctionalInterface
public interface IProcessRule<P> {

    void apply(P process);

}
