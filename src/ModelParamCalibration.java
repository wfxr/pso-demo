
import HydrologicalModel.*;

import static HydrologicalModelHelper.ModelEvaluation.NashSutcliffeEfficiency;

/**
 * 粒子群优化算法示例：
 * 以纳什效率系数为评价函数率定新安江模型包括初始状态在内的19个参数
 *
 * Created by Wenxuan on 1/17/2016.
 * Email: wenxuan-zhang@outlook.com
 */
public class ModelParamCalibration {
    // 降雨量序列
    static double[] P = new double[]{
            10, 24.1, 20.4, 18.3, 10.1, 5.5, 0.6, 3.1, 1.9, 4.6, 5,
            4.8, 36.2, 29, 6, 3.6, 0.4, 0, 0.5, 3.8, 0, 1.8, 0.2, 0.3
    };

    // 蒸发量序列
    static double[] EI = new double[]{
            0.1, 0, 0.1, 0.5, 0.7, 0.9, 0.8, 0.7, 0.5, 0.3, 0.2,
            0.1, 0, 0, 0.1, 0.6, 0.8, 1, 0.9, 0.8, 0.7, 0.5, 0.3, 0.1
    };

    // 径流量观测序列
    static double[] ObsQ = new double[]{
            88.13, 220.9, 391.36, 452.51, 427.42, 296.26, 164.35, 88.81, 109.76,
            134.55, 232.84, 306.02, 966.71, 2162.41, 1665.03, 563.39, 219.63, 84.87,
            64.58, 100.6, 137.23, 88.17, 87.44, 70.01
    };

    static XajModel xajModel = new XajModel();

    public static void main(String args[]) {
        // 定义模型各参数的有效范围
        Interval[] regionIntervals = new Interval[]{
                new Interval(5, 100),
                new Interval(50, 300),
                new Interval(5, 100),
                new Interval(0.5, 0.95),
                new Interval(0.05, 0.3),
                new Interval(0.15, 0.35),
                new Interval(0, 0.3),
                new Interval(5, 100),
                new Interval(0.5, 2),
                new Interval(0.65, 0.8),
                new Interval(0.05, 0.65),
                new Interval(0.2, 0.9),
                new Interval(0.5, 1.0),

                new Interval(0, 50),
                new Interval(20, 100),
                new Interval(40, 100),
                new Interval(0, 100),
                new Interval(0, 100),
                new Interval(0, 100),
        };

        // 创建PSO算法的问题域
        Domain domain = new Domain(regionIntervals, ModelParamCalibration::Evaluate, 1);

        // 创建PSO算法实例
        PSO pso = new PSO(domain);

        // 运行算法并存储结果
        PSOResult result = pso.Execute(1000, 100);

        // 输出结果
        System.out.println(result);
    }

    // 定义PSO算法目标函数
    public static double Evaluate(double[] params) {
        RunoffConcentrationResult result = xajModel
                .SetSoilWaterStorageParam(params[0], params[1], params[2])
                .SetEvapotranspirationParam(params[3], params[4])
                .SetRunoffGenerationParam(params[5], params[6])
                .SetSourcePartitionParam(params[7], params[8], params[9], params[10])
                .SetRunoffConcentrationParam(params[11], params[12], 537)
                .ComputeRunoffGeneration(P, EI, params[13], params[14], params[15])
                .ComputeSourcePartition(params[16], 2)
                .ComputeRunoffConcentration(params[17], params[18], 2)
                .GetResult();

        return NashSutcliffeEfficiency(ObsQ, result.Q);
    }
}
