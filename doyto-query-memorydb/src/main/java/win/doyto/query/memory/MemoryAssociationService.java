package win.doyto.query.memory;

import win.doyto.query.core.AssociationService;
import win.doyto.query.core.UniqueKey;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MemoryAssociationService
 *
 * @author f0rb on 2024/8/12
 */
@SuppressWarnings("java:S6204")
public class MemoryAssociationService<K1, K2> implements AssociationService<K1, K2> {
    static final Map<UniqueKey<String, String>, Set<? extends UniqueKey<?, ?>>> associationMap = new ConcurrentHashMap<>();

    private final Set<UniqueKey<K1, K2>> pairs;

    public MemoryAssociationService(String k1Name, String k2Name) {
        pairs = new LinkedHashSet<>();
        associationMap.put(new UniqueKey<>(k1Name, k2Name), pairs);
    }

    @Override
    public int associate(Set<UniqueKey<K1, K2>> uniqueKeys) {
        int cnt = pairs.size();
        pairs.addAll(uniqueKeys);
        return pairs.size() - cnt;
    }

    @Override
    public int dissociate(Set<UniqueKey<K1, K2>> uniqueKeys) {
        int cnt = pairs.size();
        pairs.removeAll(uniqueKeys);
        return cnt - pairs.size();
    }

    @Override
    public List<K1> queryK1ByK2(K2 k2) {
        return pairs.stream().filter(pair -> pair.getK2().equals(k2))
                    .map(UniqueKey::getK1).collect(Collectors.toList());
    }

    public List<K1> queryK1ByK2s(List<K2> k2s) {
        return pairs.stream().filter(pair -> k2s.contains(pair.getK2()))
                    .map(UniqueKey::getK1).collect(Collectors.toList());
    }

    public List<K2> queryK2ByK1s(List<K1> k1s) {
        return pairs.stream().filter(pair -> k1s.contains(pair.getK1()))
                    .map(UniqueKey::getK2).collect(Collectors.toList());
    }

    @Override
    public List<K2> queryK2ByK1(K1 k1) {
        return pairs.stream().filter(pair -> pair.getK1().equals(k1))
                    .map(UniqueKey::getK2).collect(Collectors.toList());
    }

    @Override
    public int deleteByK1(K1 k1) {
        int cnt = pairs.size();
        pairs.removeIf(pair -> pair.getK1().equals(k1));
        return cnt - pairs.size();
    }

    @Override
    public int deleteByK2(K2 k2) {
        int cnt = pairs.size();
        pairs.removeIf(pair -> pair.getK2().equals(k2));
        return cnt - pairs.size();
    }

    @Override
    public long count(Set<UniqueKey<K1, K2>> uniqueKeys) {
        return pairs.stream().filter(uniqueKeys::contains).count();
    }
}
