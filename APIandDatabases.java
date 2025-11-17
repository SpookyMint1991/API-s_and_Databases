
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Comparator;

public class APIandDatabases 
{
    public static void main(String[] args) 
    {
        HttpClient httpClient = HttpClient.newHttpClient();

        String apiUrl = "https://restcountries.com/v3.1/all?fields=name,population";

        HttpRequest request = HttpRequest.newBuilder()
        .uri(java.net.URI.create(apiUrl))
        .GET()
        .build();

        try 
        {
            HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300)
            {
                String responseBody = response.body();

                int limit = 200;
                List<String> countryList = new ArrayList<>();
                Set<String> seen = new HashSet<>();
                List<Long> populationList = new ArrayList<>();
                
                Pattern pattern = Pattern.compile("\"common\"\\s*:\\s*\"([^\"]+)\"");
                Pattern popularionPattern = Pattern.compile("\"population\"\\s*:\\s*(\\d+)");

                String[] parts = responseBody.split("\"name\"\\s*:\\s*");
                for (int i = 1; i < parts.length && countryList.size() < limit; i++)
                {
                    Matcher match = pattern.matcher(parts[i]);
                    if (match.find())
                    {
                        String commonName = match.group(1).trim();
                        Matcher matchPopulation = popularionPattern.matcher(parts[i]);
                        Long population = null;
                        if (matchPopulation.find())
                        {
                            try
                            {
                                population = Long.parseLong(matchPopulation.group(1));
                            }
                            catch (NumberFormatException nfe)
                            {
                                population = null;
                            }
                        }

                        if (!commonName.contains("?") && !seen.contains(commonName) && !commonName.isEmpty())
                        {
                            countryList.add(commonName);
                            seen.add(commonName);
                            populationList.add(population == null ? 0L : population);
                        }
                    }
                }

                String[] array = countryList.toArray(new String[0]);
                long[] populations = new long[populationList.size()];
                for (int i = 0; i < populationList.size(); i++)
                {
                    populations[i] = populationList.get(i);
                }

                Integer[] index = new Integer[array.length];
                for (int i = 0; i < array.length; i++) 
                index[i] = i;

                Arrays.sort(index, new Comparator<Integer>()
                {
                    public int compare(Integer a, Integer b)
                    {
                        return array[a].compareToIgnoreCase(array[b]);
                    }
                });

                String[] sortedNames = new String[array.length];
                long[] sortedPopulations = new long[populations.length];
                for (int i = 0; i < index.length; i++)
                {
                    sortedNames[i] = array[index[i]];
                    sortedPopulations[i] = populations[index[i]];
                }

                if (sortedNames.length == 0) {
                    System.out.println("No country names were parsed from the API response. Check parsing logic or API response format.");
                } else {
                    System.out.println("Common names sorted alphabetically:");
                    for (int i = 0; i < sortedNames.length; i++) {
                        System.out.println(sortedNames[i] + " - population: " + sortedPopulations[i]);
                    }
                }
            }
            else
            {
                System.out.println("API request failed with status code: " + response.statusCode());
            }
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        
    }
}
