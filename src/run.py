import subprocess

NUM_RUNS = 3
KVALUES = [2, 4]
THREADS = range(1, 17)
INPUT_PATH = "./input"
total_num_runs = len(KVALUES)*len(THREADS)*NUM_RUNS
runs_completed = 0
print("Total number of runs: {}".format(total_num_runs))
for kval in KVALUES:
    results = []
    for num_threads in THREADS:
        processing_time_avg = 0.0
        accumulated_time_avg = 0.0
        computed_time_avg = 0.0
        total_time_avg = 0.0
        for run_number in range(0, NUM_RUNS):
            percent_complete = round(((float(runs_completed)/float(total_num_runs))*100.0),2)
            print("[{}] Run number {} with threads {} and kval {} Percent Complete {} %"
                    .format(runs_completed + 1, run_number, num_threads, kval, percent_complete))
            cmd = ["make", "parallel", "PATH={}".format(INPUT_PATH), "KVALUE={}".format(kval), "THREADS={}".format(num_threads)]
            r = subprocess.check_output(cmd, shell=False) 
            cL = r.split('\n')
            processed_time = int(cL[1].split()[-2]) / 1000.0
            accumulated_time = int(cL[2].split()[-2]) / 1000.0
            computed_time = int(cL[3].split()[-2]) / 1000.0
            total_time = processed_time + accumulated_time + computed_time
            if run_number == 0:
                processing_time_avg = processed_time
                accumulated_time_avg = accumulated_time
                computed_time_avg = computed_time
                total_time_avg = total_time
            else:
                processing_time_avg = (processing_time_avg + processed_time) / 2.0
                accumulated_time_avg = (accumulated_time_avg + accumulated_time) / 2.0
                computed_time_avg = (computed_time_avg + computed_time) / 2.0
                total_time_avg = (total_time_avg + total_time) / 2.0
            runs_completed += 1    
        results_run = (num_threads, processing_time_avg, accumulated_time_avg, computed_time_avg, total_time_avg)
        results.append(results_run)
        print("Num Threads: {} - Processing {} s Accumulate {} s Compute {} s Total {} s"
            .format(num_threads, processing_time_avg, accumulated_time_avg, computed_time_avg, total_time_avg))

    f = open('./output/results_kval_{}_speed_up.csv'.format(kval), 'w')
    f.write("num_threads,processing_avg_s,accumulate_avg_s,compute_avg_s,total_avg_s,processing_speed_up,compute_speed_up,total_speed_up\n")
    serialRun = results[0]
    for result in results:
        processing_speedup_s = serialRun[1]/result[1]
        compute_speedup_s = serialRun[2]/result[2]
        total_speedup_s = serialRun[4]/result[4]
        f.write("{},{},{},{},{},{},{},{}\n".format(result[0],result[1],
        result[2],result[3],result[4],processing_speedup_s, compute_speedup_s, total_speedup_s))
