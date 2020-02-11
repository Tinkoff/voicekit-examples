using System.CommandLine;
using System.CommandLine.Invocation;
using System.Threading.Tasks;
using csharp.Infrastructure;

namespace Tinkoff.VoiceKit
{
    class Program
    {
        public static async Task Main(string[] args)
        {
            var rootCommand = new RootCommand("voicekit");
            var recognitionCommand = CommandLineInterface.CreateRecognitionCommand();
            var streamingRecognitionCommand = CommandLineInterface.CreateStreamingRecognitionCommand();
            var streamingSynthesisCommand = CommandLineInterface.CreateStreamingSynthesisCommand();

            rootCommand.AddCommand(recognitionCommand);
            rootCommand.AddCommand(streamingRecognitionCommand);
            rootCommand.AddCommand(streamingSynthesisCommand);

            await rootCommand.InvokeAsync(args);
        }
    }
}
