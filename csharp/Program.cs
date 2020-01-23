using System.CommandLine;
using System.CommandLine.Invocation;
using System.Threading.Tasks;

namespace Tinkoff.VoiceKit
{
    class Program
    {
        public static async Task Main(string[] args)
        {
            var commandRoot = new RootCommand("voicekit");
            var commandRecognize = CommandLineInterface.CreateRecognizeCommand();
            var commandStreamingRecognize = CommandLineInterface.CreateStreamingRecognizeCommand();
            var commandStreamingSynthesize = CommandLineInterface.CreateStreamingSynthesizeCommand();

            commandRoot.AddCommand(commandRecognize);
            commandRoot.AddCommand(commandStreamingRecognize);
            commandRoot.AddCommand(commandStreamingSynthesize);

            await commandRoot.InvokeAsync(args);
        }
    }
}
