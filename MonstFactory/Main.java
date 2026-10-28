import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

abstract class Monster {
    String name;
    int strength;
    int magic;
    int hp;

    Monster(String name, int strength, int magic, int hp) {
        this.name = name;
        this.strength = strength;
        this.magic = magic;
        this.hp = hp;
    }

    abstract int specialAttack();

}

interface Flyable {
    int flyingAttack();
}

interface OptionalAttacker {
    int optionalAttack();
}

class Slime extends Monster implements OptionalAttacker {
    Slime() {
        super("スライム", 5, 2, 20);
    }

    int specialAttack() {
        int damage = 8;
        System.out.println("スライムのねばねば攻撃！");
        return damage;
    }

    public int optionalAttack() {
        int damage = 3;
        System.out.println("スライムの分裂攻撃！");
        return damage;
    }
}

class Goblin extends Monster {
    Goblin() {
        super("ゴブリン", 8, 3, 28);
    }

    int specialAttack() {
        int damage = 13;
        System.out.println("ゴブリンの不意打ち！");
        return damage;
    }
}

class Wolf extends Monster implements OptionalAttacker {
    Wolf() {
        super("ウルフ", 10, 1, 30);
    }

    int specialAttack() {
        int damage = 15;
        System.out.println("ウルフのかみつき！");
        return damage;
    }

    public int optionalAttack() {
        int damage = 12;
        System.out.println("ウルフの急襲攻撃！");
        return damage;
    }
}

class Skeleton extends Monster {
    Skeleton() {
        super("スケルトン", 9, 5, 32);
    }

    int specialAttack() {
        int damage = 14;
        System.out.println("スケルトンの呪いの一撃！");
        return damage;
    }
}

class Orc extends Monster {
    Orc() {
        super("オーク", 14, 2, 42);
    }

    int specialAttack() {
        int damage = 20;
        System.out.println("オークの強烈な叩きつけ！");
        return damage;
    }
}

class Witch extends Monster implements Flyable {
    Witch() {
        super("ウィッチ", 5, 14, 34);
    }

    int specialAttack() {
        int damage = 18;
        System.out.println("ウィッチのダークボルト！");
        return damage;
    }

    public int flyingAttack() {
        int damage = 16;
        System.out.println(name + "は空から攻撃した！");
        return damage;
    }
}

class Golem extends Monster {
    Golem() {
        super("ゴーレム", 16, 4, 55);
    }

    int specialAttack() {
        int damage = 22;
        System.out.println("ゴーレムの岩石落とし！");
        return damage;
    }
}

class Vampire extends Monster implements Flyable, OptionalAttacker {
    Vampire() {
        super("ヴァンパイア", 13, 12, 48);
    }

    int specialAttack() {
        int damage = 19;
        System.out.println("ヴァンパイアの吸血攻撃！");
        return damage;
    }

    public int flyingAttack() {
        int damage = 20;
        System.out.println(name + "は空から攻撃した！");
        return damage;
    }

    public int optionalAttack() {
        int damage = 22;
        System.out.println("ヴァンパイアの魅惑攻撃！");
        return damage;
    }
}

class Dragon extends Monster implements Flyable {
    Dragon() {
        super("ドラゴン", 19, 16, 65);
    }

    int specialAttack() {
        int damage = 25;
        System.out.println("ドラゴンの炎のブレス！");
        return damage;
    }

    public int flyingAttack() {
        int damage = 24;
        System.out.println(name + "は空から攻撃した！");
        return damage;
    }
}

class DemonKing extends Monster {
    DemonKing() {
        super("魔王", 22, 20, 80);
    }

    int specialAttack() {
        int damage = 30;
        System.out.println("魔王の暗黒波動！");
        return damage;
    }
}

abstract class MonsterFactory {
    abstract Monster createMonster();
}

class SlimeFactory extends MonsterFactory {
    Monster createMonster() {
        return new Slime();
    }
}

class GoblinFactory extends MonsterFactory {
    Monster createMonster() {
        return new Goblin();
    }
}

class WolfFactory extends MonsterFactory {
    Monster createMonster() {
        return new Wolf();
    }
}

class SkeletonFactory extends MonsterFactory {
    Monster createMonster() {
        return new Skeleton();
    }
}

class OrcFactory extends MonsterFactory {
    Monster createMonster() {
        return new Orc();
    }
}

class WitchFactory extends MonsterFactory {
    Monster createMonster() {
        return new Witch();
    }
}

class GolemFactory extends MonsterFactory {
    Monster createMonster() {
        return new Golem();
    }
}

class VampireFactory extends MonsterFactory {
    Monster createMonster() {
        return new Vampire();
    }
}

class DragonFactory extends MonsterFactory {
    Monster createMonster() {
        return new Dragon();
    }
}

class DemonKingFactory extends MonsterFactory {
    Monster createMonster() {
        return new DemonKing();
    }
}

class Hero {
    int strength = 12;
    int magic = 15;
    int hp = 100;
    int maxHp = 100;
    int potions = 3;
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        List<MonsterFactory> monsterList = new ArrayList<>();
        monsterList.add(new SlimeFactory());
        monsterList.add(new GoblinFactory());
        monsterList.add(new WolfFactory());
        monsterList.add(new SkeletonFactory());
        monsterList.add(new OrcFactory());
        monsterList.add(new WitchFactory());
        monsterList.add(new GolemFactory());
        monsterList.add(new VampireFactory());
        monsterList.add(new DragonFactory());
        monsterList.add(new DemonKingFactory());

        System.out.println("=== モンスター一覧 ===");
        for (int i = 0; i < monsterList.size(); i++) {
            Monster preview = monsterList.get(i).createMonster();
            System.out.println((i + 1) + ". " + preview.name
                    + " (腕力:" + preview.strength + " 魔力:" + preview.magic
                    + " HP:" + preview.hp + ")");
        }

        int choice = readNumber(scanner, "戦うモンスターの番号を選んでください: ", 1, monsterList.size());
        Monster monster = monsterList.get(choice - 1).createMonster();
        Hero hero = new Hero();
        System.out.println("\n勇者のチームが " + monster.name + " と戦います！");

        while (hero.hp > 0 && monster.hp > 0) {
            System.out.println("\n勇者 HP:" + hero.hp + "/" + hero.maxHp
                    + "  MP:" + hero.magic + "  回復薬:" + hero.potions);
            System.out.println(monster.name + " HP:" + monster.hp);
            System.out.println("1. 戦う  2. 戦闘魔法  3. 回復");
            int action = readNumber(scanner, "行動を選んでください: ", 1, 3);

            if (action == 1) {
                int damage = hero.strength;
                monster.hp -= damage;
                System.out.println("勇者の攻撃！ " + monster.name + "に" + damage + "ダメージ。");
            } else if (action == 2) {
                if (hero.magic >= 5) {
                    int damage = hero.magic + 8;
                    hero.magic -= 5;
                    monster.hp -= damage;
                    System.out.println("勇者はファイアを唱えた！ " + monster.name + "に" + damage + "ダメージ。");
                } else {
                    System.out.println("魔力が足りません。今回は攻撃できません。");
                }
            } else {
                if (hero.potions > 0) {
                    int before = hero.hp;
                    hero.hp = Math.min(hero.maxHp, hero.hp + 30);
                    hero.potions--;
                    System.out.println("回復薬を使った！ HPが" + (hero.hp - before) + "回復。");
                } else {
                    System.out.println("回復薬がありません！");
                }
            }

            if (monster.hp > 0) {
                int damage;
                int attackRoll = random.nextInt(4);
                if (monster instanceof OptionalAttacker && attackRoll == 0) {
                    damage = ((OptionalAttacker) monster).optionalAttack();
                } else if (monster instanceof Flyable && attackRoll == 1) {
                    damage = ((Flyable) monster).flyingAttack();
                } else if (attackRoll == 2) {
                    damage = monster.specialAttack();
                } else {
                    damage = monster.strength;
                    System.out.println(monster.name + "の攻撃！");
                }
                hero.hp -= damage;
                System.out.println("勇者は" + damage + "ダメージを受けた。");
            }
        }

        if (hero.hp > 0) {
            System.out.println("\n勝利！ " + monster.name + "を倒しました。");
        } else {
            System.out.println("\n勇者は倒れてしまった……。ゲームオーバー。");
        }
        scanner.close();
    }

    static int readNumber(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                int number = Integer.parseInt(input);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println(min + "から" + max + "までの数字を入力してください。");
        }
    }
}
